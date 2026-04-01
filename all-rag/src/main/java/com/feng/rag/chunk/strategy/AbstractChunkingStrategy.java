package com.feng.rag.chunk.strategy;

import com.feng.rag.chunk.model.Chunk;
import com.feng.rag.chunk.model.ChunkingOptions;
import com.feng.rag.chunk.model.ChunkingResult;
import com.feng.rag.datasource.common.DocumentParseResult;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分块策略抽象基类 —— 提供通用工具方法和标准流程
 */
@Slf4j
public abstract class AbstractChunkingStrategy implements ChunkingStrategy {

    // 句子边界正则（支持中英文）
    protected static final Pattern SENTENCE_PATTERN = Pattern.compile(
        "[^。！？.!?\\n]+[。！？.!?\\n]*"
    );

    // 段落分隔正则
    protected static final Pattern PARAGRAPH_PATTERN = Pattern.compile(
        "\\n\\s*\\n|\\r\\n\\s*\\r\\n"
    );

    // 章节标题正则
    protected static final Pattern HEADING_PATTERN = Pattern.compile(
        "^#{1,6}\\s+|^\\d+[\\.\\)\\s]+|^第[一二三四五六七八九十\\d]+[章节篇]|^\\*\\*[^*]+\\*\\*"
    );

    // 代码块标记
    protected static final Pattern CODE_BLOCK_PATTERN = Pattern.compile(
        "^```[\\w]*$|^~~~[\\w]*$"
    );

    @Override
    public ChunkingResult chunk(DocumentParseResult document, ChunkingOptions options) {
        Instant startTime = Instant.now();
        if (document == null || document.getContent() == null) {
            log.error("[{}] 文档内容为空", getStrategyName());
            return ChunkingResult.failed(
                document != null ? document.getSourceId() : null,
                document != null ? document.getFileName() : null,
                "文档内容为空"
            );
        }
        String content = document.getContent();
        String docId = document.getSourceId();
        String docName = document.getFileName();
        log.info("[{}] 开始分块: document={}, length={}", getStrategyName(), docName, content.length());

        try {
            // 执行实际分块逻辑（由子类实现）
            List<Chunk> chunks = doChunk(content, docId, docName, options);
            // 后处理：链接相邻分块、计算质量分、过滤等
            chunks = postProcessChunks(chunks, content, options);

            Instant endTime = Instant.now();

            ChunkingResult result = ChunkingResult.builder()
                .documentId(docId)
                .documentName(docName)
                .chunks(chunks)
                .strategyName(getStrategyName())
                .options(options)
                .status(ChunkingResult.Status.SUCCESS)
                .originalTextLength(content.length())
                .startTime(startTime)
                .endTime(endTime)
                .build();
            log.info("[{}] 分块完成: document={}, chunks={}, avgSize={}, durationMs={}",
                getStrategyName(), docName, chunks.size(),
                result.getAverageChunkSize(), result.getDurationMs());

            return result;
        } catch (Exception e) {
            log.error("[{}] 分块失败: document={}", getStrategyName(), docName, e);
            return ChunkingResult.failed(docId, docName, e.getMessage());
        }
    }

    @Override
    public ChunkingResult chunkText(String text, String documentId, String documentName,
                                     ChunkingOptions options) {
        DocumentParseResult doc = DocumentParseResult.builder()
            .sourceId(documentId)
            .fileName(documentName)
            .content(text)
            .contentLength(text.length())
            .status(DocumentParseResult.ParseStatus.SUCCESS)
            .build();
        return chunk(doc, options);
    }

    /**
     * 子类实现的具体分块逻辑
     */
    protected abstract List<Chunk> doChunk(String content, String docId, String docName,
                                           ChunkingOptions options);

    /**
     * 分块后处理
     */
    protected List<Chunk> postProcessChunks(List<Chunk> chunks, String originalContent,
                                            ChunkingOptions options) {
        if (chunks.isEmpty()) {
            return chunks;
        }
        // 1. 生成 UUID
        for (Chunk chunk : chunks) {
            chunk.setChunkId(UUID.randomUUID().toString());
        }
        // 2. 链接相邻分块
        linkAdjacentChunks(chunks);
        // 3. 计算质量评分
        if (options.isEnableQualityFilter()) {
            for (Chunk chunk : chunks) {
                calculateQualityScore(chunk, options);
            }
        }
        // 4. 过滤低质量分块
        // if (options.isFilterShortChunks()) {
        //     chunks = chunks.stream()
        //         .filter(c -> c.getContentLength() >= options.getMinChunkSize())
        //         .toList();
        // }
        // // 5. 过滤质量不达标分块
        // if (options.isEnableQualityFilter()) {
        //     chunks = chunks.stream()
        //         .filter(c -> c.getQualityScore() >= options.getMinQualityScore())
        //         .toList();
        // }
        // 6. 设置索引
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).setChunkIndex(i);
        }
        return chunks;
    }

    /**
     * 链接相邻分块（设置 prev/next）
     */
    protected void linkAdjacentChunks(List<Chunk> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if (i > 0) {
                chunk.setPrevChunkId(chunks.get(i - 1).getChunkId());
            }
            if (i < chunks.size() - 1) {
                chunk.setNextChunkId(chunks.get(i + 1).getChunkId());
            }
        }
    }

    /**
     * 计算分块质量评分
     */
    protected void calculateQualityScore(Chunk chunk, ChunkingOptions options) {
        double score = 1.0;
        String content = chunk.getContent();
        // 1. 长度因子（过短或过长都扣分）
        int length = content.length();
        int targetSize = options.getTargetChunkSize();
        if (length < options.getMinChunkSize()) {
            score *= 0.5;
        } else if (length > options.getMaxChunkSize()) {
            score *= 0.7;
        } else if (Math.abs(length - targetSize) < targetSize * 0.2) {
            score *= 1.0;  // 接近目标大小
        }
        // 2. 句子完整性
        if (!chunk.isStartsWithCompleteSentence()) {
            score *= 0.9;
        }
        if (!chunk.isEndsWithCompleteSentence()) {
            score *= 0.9;
        }
        // 3. 段落边界因子
        if (chunk.isCrossesParagraphBoundary()) {
            score *= 0.85;
        }
        // 4. 内容密度（避免过多空白字符）
        if (!content.isEmpty()) {
            double nonWhitespaceRatio = (double) content.replaceAll("\\s", "").length() / content.length();
            score *= (0.5 + 0.5 * nonWhitespaceRatio);  // 0.5 - 1.0
        }
        // 5. 信息熵因子（避免重复字符）
        double entropy = calculateEntropy(content);
        score *= Math.min(1.0, entropy / 3.0);  // 归一化到 0-1
        chunk.setQualityScore(Math.round(score * 100) / 100.0);
    }

    /**
     * 计算字符串信息熵
     */
    protected double calculateEntropy(String text) {
        if (text == null || text.isEmpty()) return 0;
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : text.toCharArray()) {
            freq.merge(c, 1, Integer::sum);
        }
        double entropy = 0;
        int len = text.length();
        for (int count : freq.values()) {
            double p = (double) count / len;
            entropy -= p * Math.log(p) / Math.log(2);
        }
        return entropy;
    }

    /**
     * 在指定位置查找最佳切分点（避免切断句子）
     */
    protected int findBestSplitPoint(String text, int targetPosition, boolean preferBackward,
                                      ChunkingOptions options) {
        int length = text.length();
        if (targetPosition <= 0) return 0;
        if (targetPosition >= length) return length;
        int searchRange = Math.min(100, options.getTargetChunkSize() / 5);
        int start = Math.max(0, targetPosition - searchRange);
        int end = Math.min(length, targetPosition + searchRange);
        // 优先查找段落边界
        if (options.isRespectParagraphBoundaries()) {
            String searchArea = text.substring(start, end);
            Matcher matcher = PARAGRAPH_PATTERN.matcher(searchArea);
            int bestPos = -1;
            int minDist = Integer.MAX_VALUE;
            while (matcher.find()) {
                int pos = start + matcher.end();
                int dist = Math.abs(pos - targetPosition);
                if (dist < minDist) {
                    minDist = dist;
                    bestPos = pos;
                }
            }
            if (bestPos != -1 && minDist < searchRange) {
                return bestPos;
            }
        }
        // 其次查找句子边界
        if (options.isRespectSentenceBoundaries()) {
            String searchArea = text.substring(start, end);
            Matcher matcher = SENTENCE_PATTERN.matcher(searchArea);
            int bestPos = -1;
            int minDist = Integer.MAX_VALUE;
            while (matcher.find()) {
                int endPos = start + matcher.end();
                int dist = Math.abs(endPos - targetPosition);
                if (dist < minDist) {
                    minDist = dist;
                    bestPos = endPos;
                }
            }
            if (bestPos != -1 && minDist < searchRange) {
                return bestPos;
            }
        }
        // 退而求其次，查找空格或换行
        for (int offset = 0; offset < searchRange; offset++) {
            if (preferBackward && targetPosition - offset > 0) {
                char c = text.charAt(targetPosition - offset - 1);
                if (c == ' ' || c == '\n' || c == '\t') {
                    return targetPosition - offset;
                }
            }
            if (!preferBackward && targetPosition + offset < length) {
                char c = text.charAt(targetPosition + offset);
                if (c == ' ' || c == '\n' || c == '\t') {
                    return targetPosition + offset;
                }
            }
        }
        // 最终 fallback
        return targetPosition;
    }

    /**
     * 检测是否以完整句子开始
     */
    protected boolean isCompleteSentenceStart(String text) {
        if (text == null || text.isEmpty()) return true;
        char first = text.trim().charAt(0);
        // 大写字母、中文、数字、引号开头认为是完整句子开始
        return Character.isUpperCase(first) ||
               (first >= '\u4e00' && first <= '\u9fa5') ||
               Character.isDigit(first) ||
               first == '"' || first == '"' || first == '\'' || first == '（' || first == '(';
    }

    /**
     * 检测是否以完整句子结束
     */
    protected boolean isCompleteSentenceEnd(String text) {
        if (text == null || text.isEmpty()) return true;
        String trimmed = text.trim();
        char last = trimmed.charAt(trimmed.length() - 1);
        // 句号、问号、感叹号、引号、括号结尾
        return last == '。' || last == '？' || last == '！' ||
               last == '.' || last == '?' || last == '!' ||
               last == '"' || last == '"' || last == '\'' ||
               last == '）' || last == ')';
    }

    /**
     * 检测分块类型
     */
    protected Chunk.ChunkType detectChunkType(String content) {
        String trimmed = content.trim();
        String firstLine = trimmed.split("\\n")[0];

        // 代码块检测
        if (CODE_BLOCK_PATTERN.matcher(firstLine).find() ||
            (trimmed.contains("{") && trimmed.contains("}") && trimmed.contains(";"))) {
            return Chunk.ChunkType.CODE_BLOCK;
        }

        // 标题检测
        if (HEADING_PATTERN.matcher(firstLine).matches()) {
            if (firstLine.startsWith("#")) {
                int level = 0;
                for (char c : firstLine.toCharArray()) {
                    if (c == '#') level++;
                    else break;
                }
                return level == 1 ? Chunk.ChunkType.TITLE : Chunk.ChunkType.HEADING;
            }
            return Chunk.ChunkType.HEADING;
        }

        // 列表项检测
        if (firstLine.matches("^[-*+•]\\s|^\\d+[\\.\\)]\\s")) {
            return Chunk.ChunkType.LIST_ITEM;
        }

        // 表格检测
        if (trimmed.contains("|") && trimmed.lines().count() >= 2) {
            long separatorLines = trimmed.lines()
                .filter(line -> line.matches("^\\|[-:|\\s]+\\|$"))
                .count();
            if (separatorLines > 0) {
                return Chunk.ChunkType.TABLE;
            }
        }

        // 引用检测
        if (firstLine.startsWith(">") || firstLine.startsWith("【引用")) {
            return Chunk.ChunkType.QUOTE;
        }

        return Chunk.ChunkType.PARAGRAPH;
    }

    /**
     * 提取章节路径（基于标题层级）
     */
    protected List<String> extractSectionPath(String content, List<String> currentPath) {
        List<String> path = new ArrayList<>(currentPath);

        String firstLine = content.trim().split("\\n")[0];
        if (HEADING_PATTERN.matcher(firstLine).matches()) {
            // 清理标题标记
            String cleanTitle = firstLine
                .replaceAll("^#{1,6}\\s+", "")
                .replaceAll("^\\*\\*([^*]+)\\*\\*$", "$1")
                .trim();

            // 根据层级调整路径
            int level = 0;
            if (firstLine.startsWith("#")) {
                for (char c : firstLine.toCharArray()) {
                    if (c == '#') level++;
                    else break;
                }
            }

            // 调整路径深度
            while (path.size() >= level && !path.isEmpty()) {
                path.remove(path.size() - 1);
            }
            path.add(cleanTitle);
        }

        return path;
    }
}
