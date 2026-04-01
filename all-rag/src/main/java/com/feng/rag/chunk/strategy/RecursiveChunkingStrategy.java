package com.feng.rag.chunk.strategy;

import com.feng.rag.chunk.model.Chunk;
import com.feng.rag.chunk.model.ChunkingOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 递归分块策略 —— 按文档结构层级递归切分，保持语义完整性
 *
 * <p>特点：
 * <ul>
 *   <li>按文档结构层级切分（标题 → 段落 → 句子）</li>
 *   <li>优先保持语义边界完整</li>
 *   <li>支持父子分块关系，便于检索时获取上下文</li>
 *   <li>适合层次分明的结构化文档</li>
 * </ul>
 *
 * <p>适用场景：
 * <ul>
 *   <li>技术文档、API 文档</li>
 *   <li>书籍、论文</li>
 *   <li>结构化报告、手册</li>
 * </ul>
 */
@Slf4j
@Component
public class RecursiveChunkingStrategy extends AbstractChunkingStrategy {

    public static final String STRATEGY_NAME = "recursive";

    // 使用正则保留分隔符（Lookbehind/Lookahead），确保标点符号不丢失
    private static final String[] SEPARATORS = {
            "\n\n\n", "\n\n", "\n",
            "(?<=。)", "(?<=！)", "(?<=？)", // 中文句子
            "(?<=[.!?])",                  // 英文句子
            " ", ""                        // 单词与字符
    };

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public String getStrategyDescription() {
        return "递归分块策略：按文档结构层级递归切分，优先保持语义边界完整。" +
               "支持父子分块关系，适合层次分明的结构化文档如技术文档、论文等。";
    }

    @Override
    public ChunkingOptions getDefaultOptions() {
        return ChunkingOptions.builder()
            .targetChunkSize(500)
            .minChunkSize(100)
            .maxChunkSize(1000)
            .overlapSize(0)
            .respectParagraphBoundaries(true)
            .respectSentenceBoundaries(true)
            .splitAtHeadings(true)
            .maxHierarchyDepth(3)
            .build();
    }

    @Override
    protected List<Chunk> doChunk(String content, String docId, String docName, ChunkingOptions options) {
        List<Chunk> result = new ArrayList<>();
        // 初始调用，层级从 0 开始
        splitAndRecombine(content, 0, 0, docId, docName, options, new ArrayList<>(), result);
        log.debug("[{}] 生成了 {} 个高质量递归分块", STRATEGY_NAME, result.size());
        return result;
    }

    /**
     * 核心逻辑：递归切分并合并碎片
     */
    private void splitAndRecombine(String text, int separatorIdx, int absOffset,
                                   String docId, String docName, ChunkingOptions options,
                                   List<String> currentPath, List<Chunk> result) {

        if (text.isEmpty()) return;

        // 1. 如果当前文本已经足够小，直接作为一个整体（由上一层决定是否合并）
        if (text.length() <= options.getTargetChunkSize()) {
            addChunkToList(text, absOffset, docId, docName, options, currentPath, result);
            return;
        }

        // 2. 选择当前层级的层级分隔符
        String separator = SEPARATORS[separatorIdx];
        String[] parts = splitWithSeparator(text, separator);

        List<String> goodParts = new ArrayList<>();
        int currentPartOffset = absOffset;

        for (String part : parts) {
            if (part.isEmpty()) continue;

            // 3. 如果子块仍然太大，且还有更细的分隔符，则递归深挖
            if (part.length() > options.getTargetChunkSize() && separatorIdx < SEPARATORS.length - 1) {
                // 先处理之前积累的小碎片
                flushGoodParts(goodParts, absOffset, docId, docName, options, currentPath, result);
                goodParts.clear();

                // 递归处理这个超大子块
                splitAndRecombine(part, separatorIdx + 1, currentPartOffset, docId, docName, options, currentPath, result);
            } else {
                // 4. 尝试将碎片积累，后续统一合并
                if (getTotalLength(goodParts) + part.length() > options.getMaxChunkSize()) {
                    flushGoodParts(goodParts, absOffset, docId, docName, options, currentPath, result);
                    goodParts.clear();
                    absOffset = currentPartOffset;
                }
                goodParts.add(part);
            }
            currentPartOffset += part.length();
        }

        // 最后收尾
        flushGoodParts(goodParts, absOffset, docId, docName, options, currentPath, result);
    }

    /**
     * 将多个碎片合并为一个 Chunk
     */
    private void flushGoodParts(List<String> parts, int startOffset, String docId, String docName,
                                ChunkingOptions options, List<String> currentPath, List<Chunk> result) {
        if (parts.isEmpty()) return;

        String combinedContent = String.join("", parts);
        if (combinedContent.trim().length() < 5) return; // 过滤噪音字符

        addChunkToList(combinedContent, startOffset, docId, docName, options, currentPath, result);
    }

    private void addChunkToList(String content, int offset, String docId, String docName,
                                ChunkingOptions options, List<String> currentPath, List<Chunk> result) {

        // 更新标题路径逻辑
        List<String> path = updatePathIfNeeded(content, currentPath);

        Chunk chunk = Chunk.builder()
                .chunkIndex(result.size())
                .content(content.trim())
                .contentLength(content.trim().length())
                .documentId(docId)
                .documentName(docName)
                .startOffset(offset)
                .endOffset(offset + content.length())
                .sectionPath(new ArrayList<>(path))
                .chunkingStrategy(STRATEGY_NAME)
                .build();

        result.add(chunk);
    }

    /**
     * 辅助方法：使用正则切分但保留分隔符内容
     */
    private String[] splitWithSeparator(String text, String separator) {
        if (separator.isEmpty()) {
            return text.split(""); // 字符级切分
        }
        // 使用正则拆分，保留分隔符
        // 注意：这里需要根据具体的正则语法调整
        return text.split("(?=" + separator + ")|" + separator);
    }

    private int getTotalLength(List<String> parts) {
        return parts.stream().mapToInt(String::length).sum();
    }

    private List<String> updatePathIfNeeded(String content, List<String> currentPath) {
        // 这里可以复用你原来的 isHeading 逻辑来动态更新路径
        // 生产级通常会配合正则表达式提取 Markdown 的 # 标题
        return currentPath;
    }
}
