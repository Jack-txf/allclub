package com.feng.rag.chunk.strategy;

import com.feng.rag.chunk.model.Chunk;
import com.feng.rag.chunk.model.ChunkingOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 固定大小分块策略 —— 按固定字符数切分，支持重叠
 *
 * <p>特点：
 * <ul>
 *   <li>实现简单，分块大小均匀</li>
 *   <li>适合对上下文要求不高的场景</li>
 *   <li>可能切断句子，需配合重叠使用</li>
 * </ul>
 *
 * <p>适用场景：
 * <ul>
 *   <li>日志文件分析</li>
 *   <li>代码片段检索</li>
 *   <li>对精确度要求不高的全文检索</li>
 * </ul>
 */
@Slf4j
@Component
public class FixedSizeChunkingStrategy extends AbstractChunkingStrategy {

    public static final String STRATEGY_NAME = "fixed_size";

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public String getStrategyDescription() {
        return "固定大小分块策略：按固定字符数切分，支持重叠区域保持上下文连续性。" +
               "实现简单高效，适合对上下文完整性要求不高的场景。";
    }

    @Override
    public ChunkingOptions getDefaultOptions() {
        return ChunkingOptions.builder()
            .targetChunkSize(500)
            .minChunkSize(100)
            .maxChunkSize(1000)
            .overlapSize(50)
            .respectSentenceBoundaries(false)
            .respectParagraphBoundaries(false)
            .build();
    }

    @Override
    protected List<Chunk> doChunk(String content, String docId, String docName, ChunkingOptions options) {
        List<Chunk> chunks = new ArrayList<>();
        int contentLength = content.length();

        int targetSize = options.getTargetChunkSize();
        int overlapSize = options.getOverlapSize();
        int maxSize = options.getMaxChunkSize();

        int position = 0;
        int chunkIndex = 0;

        while (position < contentLength) {
            // 计算当前分块的结束位置
            int endPos = Math.min(position + targetSize, contentLength);

            // 如果还没到末尾且超过最大大小，查找更好的切分点
            if (endPos < contentLength && endPos - position > maxSize) {
                endPos = findBestSplitPoint(content, position + maxSize, true, options);
            }

            // 如果还没到文档末尾，尝试找更好的切分点
            if (endPos < contentLength) {
                endPos = findBestSplitPoint(content, endPos, true, options);
            }

            // 确保至少有一些内容
            if (endPos <= position) {
                endPos = Math.min(position + targetSize, contentLength);
            }

            String chunkContent = content.substring(position, endPos).trim();

            if (!chunkContent.isEmpty()) {
                Chunk chunk = Chunk.builder()
                    .chunkIndex(chunkIndex++)
                    .content(chunkContent)
                    .contentLength(chunkContent.length())
                    .documentId(docId)
                    .documentName(docName)
                    .startOffset(position)
                    .endOffset(endPos)
                    .chunkType(detectChunkType(chunkContent))
                    .startsWithCompleteSentence(isCompleteSentenceStart(chunkContent))
                    .endsWithCompleteSentence(isCompleteSentenceEnd(chunkContent))
                    .chunkingStrategy(STRATEGY_NAME)
                    .build();

                chunks.add(chunk);
            }

            if (endPos >= contentLength) break; // 如果已经到文档末尾，结束循环
            // 下一个分块的起始位置（考虑重叠）
            position = endPos - overlapSize;
            if (position <= 0 || position >= contentLength) {
                position = endPos;
            }

            // 防止死循环
            if (position <= 0 || position == endPos) {
                position = endPos;
            }
        }

        log.debug("[{}] 生成了 {} 个固定大小分块", STRATEGY_NAME, chunks.size());
        return chunks;
    }
}
