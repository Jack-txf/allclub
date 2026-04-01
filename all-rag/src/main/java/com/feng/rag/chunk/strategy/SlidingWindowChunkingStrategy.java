package com.feng.rag.chunk.strategy;

import com.feng.rag.chunk.model.Chunk;
import com.feng.rag.chunk.model.ChunkingOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 滑动窗口分块策略 —— 固定窗口大小，按步长滑动
 *
 * <p>特点：
 * <ul>
 *   <li>相邻分块有大量重叠，保证上下文连续性</li>
 *   <li>召回率高，不会遗漏跨分块的信息</li>
 *   <li>分块数量多，存储和计算成本高</li>
 * </ul>
 *
 * <p>适用场景：
 * <ul>
 *   <li>需要极高召回率的场景（如法律条文检索）</li>
 *   <li>短文本、关键信息密集的内容</li>
 *   <li>对响应速度要求不高的场景</li>
 * </ul>
 */
@Slf4j
@Component
public class SlidingWindowChunkingStrategy extends AbstractChunkingStrategy {

    public static final String STRATEGY_NAME = "sliding_window";

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public String getStrategyDescription() {
        return "滑动窗口分块策略：固定窗口大小，按步长滑动产生重叠分块。" +
               "召回率高，适合对精确度要求极高的场景如法律条文检索。";
    }

    @Override
    public ChunkingOptions getDefaultOptions() {
        return ChunkingOptions.builder()
            .targetChunkSize(400)
            .minChunkSize(100)
            .maxChunkSize(500)
            .overlapSize(200)  // 默认 50% 重叠
            .respectSentenceBoundaries(true)
            .build();
    }

    @Override
    protected List<Chunk> doChunk(String content, String docId, String docName, ChunkingOptions options) {
        List<Chunk> chunks = new ArrayList<>();
        int contentLength = content.length();
        int targetSize = options.getTargetChunkSize();
        int overlap = options.getOverlapSize();

        int position = 0;
        int chunkIndex = 0;

        while (position < contentLength) {
            // 1. 确定初始结束位置
            int endPos = Math.min(position + targetSize, contentLength);

            // 2. 寻找最佳切分点（如句号、换行）
            if (endPos < contentLength) {
                int bestSplit = findBestSplitPoint(content, endPos, true, options);
                // 防止寻找到的切分点比起点还早（极端情况防御）
                if (bestSplit > position) {
                    endPos = bestSplit;
                }
            }

            // 3. 提取并校验分块
            String chunkContent = content.substring(position, endPos).trim();

            // 只有满足最小长度才添加，或者是最后一段
            if (!chunkContent.isEmpty() && (chunkContent.length() >= options.getMinChunkSize() || endPos == contentLength)) {
                chunks.add(buildChunk(chunkContent, position, endPos, chunkIndex++, docId, docName));
            }

            // 4. 计算下一个起始位置（核心修改：基于当前切分点计算 Overlap）
            if (endPos >= contentLength) {
                break; // 已处理完毕
            }

            // 下一次的起点 = 当前终点 - 重叠量
            int nextPosition = endPos - overlap;

            // 防御：确保下一次起点至少比当前起点进步一步，防止死循环
            position = Math.max(nextPosition, position + 1);

            // 5. 尾部处理优化：如果剩余内容太少，直接合并到当前最后一块并退出
            if (contentLength - position < options.getMinChunkSize() && !chunks.isEmpty()) {
                Chunk lastChunk = chunks.get(chunks.size() - 1);
                String remaining = content.substring(lastChunk.getEndOffset(), contentLength).trim();
                if (!remaining.isEmpty()) {
                    lastChunk.setContent(lastChunk.getContent() + " " + remaining);
                    lastChunk.setContentLength(lastChunk.getContent().length());
                    lastChunk.setEndOffset(contentLength);
                }
                break;
            }
        }

        log.debug("[{}] 生成了 {} 个滑动窗口分块", STRATEGY_NAME, chunks.size());
        return chunks;
    }

    /**
     * 提取对象构建逻辑，保持代码整洁
     */
    private Chunk buildChunk(String content, int start, int end, int index, String docId, String docName) {
        return Chunk.builder()
                .chunkIndex(index)
                .content(content)
                .contentLength(content.length())
                .documentId(docId)
                .documentName(docName)
                .startOffset(start)
                .endOffset(end)
                .chunkType(detectChunkType(content))
                .startsWithCompleteSentence(isCompleteSentenceStart(content))
                .endsWithCompleteSentence(isCompleteSentenceEnd(content))
                .chunkingStrategy(STRATEGY_NAME)
                .build();
    }
}
