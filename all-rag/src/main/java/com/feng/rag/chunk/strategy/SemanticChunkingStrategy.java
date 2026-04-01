package com.feng.rag.chunk.strategy;

import com.feng.rag.chunk.model.Chunk;
import com.feng.rag.chunk.model.ChunkingOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语义分块策略 —— 基于句子语义完整性进行分块
 *
 * <p>特点：
 * <ul>
 *   <li>以句子为最小单位</li>
 *   <li>相邻句子语义相似度高则合并</li>
 *   <li>在语义边界处切分，保持主题一致性</li>
 *   <li>适合主题明确、段落较长的文档</li>
 * </ul>
 *
 * <p>适用场景：
 * <ul>
 *   <li>学术论文、研究报告</li>
 *   <li>主题明确的博客文章</li>
 *   <li>需要保持主题连贯性的长文本</li>
 * </ul>
 */
@Slf4j
@Component
public class SemanticChunkingStrategy extends AbstractChunkingStrategy {

    public static final String STRATEGY_NAME = "semantic";

    // 句子结束标点
    private static final Pattern SENTENCE_END = Pattern.compile("[。！？.!?]\\s*");

    // 主题转换信号词
    private static final Set<String> TOPIC_SHIFT_MARKERS = Set.of(
        "然而", "但是", "不过", "另一方面", "相反",
        "此外", "另外", "除此之外", "同时",
        "因此", "所以", "综上所述", "总之",
        "首先", "其次", "再次", "最后",
        "第一", "第二", "第三", "第四", "第五",
        "but", "however", "therefore", "thus", "moreover", "furthermore",
        "in addition", "on the other hand", "conversely", "in conclusion"
    );

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public String getStrategyDescription() {
        return "语义分块策略：基于句子语义完整性进行分块，" +
               "在语义边界处切分以保持主题一致性。适合学术论文、研究报告等主题明确的文档。";
    }

    @Override
    public ChunkingOptions getDefaultOptions() {
        return ChunkingOptions.builder()
            .targetChunkSize(500)
            .minChunkSize(150)
            .maxChunkSize(1000)
            .overlapSize(0)
            .respectSentenceBoundaries(true)
            .semanticSimilarityThreshold(0.6)
            .build();
    }

    @Override
    protected List<Chunk> doChunk(String content, String docId, String docName, ChunkingOptions options) {
        List<Chunk> chunks = new ArrayList<>();

        // 1. 将文本分割为句子
        List<Sentence> sentences = splitIntoSentences(content);

        if (sentences.isEmpty()) {
            return chunks;
        }

        // 2. 语义聚类：将语义相关的句子合并为分块
        List<String> currentSectionPath = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int currentChunkStart = sentences.getFirst().startOffset;
        int sentenceIndex = 0;

        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            String nextSentence = (i < sentences.size() - 1) ? sentences.get(i + 1).text : null;

            // 更新章节路径
            if (isHeading(sentence.text)) {
                String heading = extractHeading(sentence.text);
                int level = getHeadingLevel(sentence.text);
                while (currentSectionPath.size() >= level) {
                    if (!currentSectionPath.isEmpty()) {
                        currentSectionPath.removeLast();
                    }
                }
                currentSectionPath.add(heading);
            }

            // 检查是否应该在当前句子后切分
            boolean shouldSplit = false;

            // 检查当前分块大小
            int currentSize = currentChunk.length();
            if (currentSize > 0) {
                currentSize += 1; // 空格
            }
            currentSize += sentence.text.length();

            // 如果加入当前句子会超过目标大小
            if (currentSize > options.getTargetChunkSize() && currentChunk.length() >= options.getMinChunkSize()) {
                shouldSplit = true;
            }

            // 如果超过最大大小，必须切分
            if (currentSize > options.getMaxChunkSize()) {
                shouldSplit = true;
            }

            // 检查语义边界信号
            if (!shouldSplit && !currentChunk.isEmpty()) {
                // 当前句子以转换词开头
                if (isTopicShiftMarker(sentence.text)) {
                    // 检查当前分块大小是否合适
                    if (currentChunk.length() >= options.getMinChunkSize()) {
                        shouldSplit = true;
                    }
                }

                // 下一句子是标题，应该切分
                if (nextSentence != null && isHeading(nextSentence)) {
                    if (currentChunk.length() >= options.getMinChunkSize()) {
                        shouldSplit = true;
                    }
                }

                // 段落边界
                if (sentence.isParagraphEnd && currentChunk.length() >= options.getMinChunkSize()) {
                    shouldSplit = true;
                }
            }

            // 执行切分
            if (shouldSplit && !currentChunk.isEmpty()) {
                // 先保存当前分块
                saveChunk(chunks, currentChunk.toString(), docId, docName,
                    currentChunkStart, sentence.startOffset,
                    chunks.size(), new ArrayList<>(currentSectionPath), options);

                // 开始新分块
                currentChunk = new StringBuilder(sentence.text);
                currentChunkStart = sentence.startOffset;
            } else {
                // 追加到当前分块
                if (!currentChunk.isEmpty()) {
                    currentChunk.append(" ");
                }
                currentChunk.append(sentence.text);
            }

            sentenceIndex++;
        }

        // 保存最后一个分块
        if (!currentChunk.isEmpty()) {
            Sentence lastSentence = sentences.getLast();
            saveChunk(chunks, currentChunk.toString(), docId, docName,
                currentChunkStart, lastSentence.endOffset,
                chunks.size(), new ArrayList<>(currentSectionPath), options);
        }

        log.debug("[{}] 生成了 {} 个语义分块", STRATEGY_NAME, chunks.size());
        return chunks;
    }

    /**
     * 将文本分割为句子
     */
    private List<Sentence> splitIntoSentences(String text) {
        List<Sentence> sentences = new ArrayList<>();

        // 按段落预分割
        String[] paragraphs = text.split("\n\s*\n|\r\n\s*\r\n");
        int globalOffset = 0;

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                globalOffset += paragraph.length() + 2;
                continue;
            }

            // 在段落内按句子切分
            Matcher matcher = SENTENCE_END.matcher(paragraph);
            int lastEnd = 0;

            while (matcher.find()) {
                int sentenceEnd = matcher.end();
                String sentenceText = paragraph.substring(lastEnd, sentenceEnd).trim();

                if (!sentenceText.isEmpty()) {
                    sentences.add(new Sentence(
                        sentenceText,
                        globalOffset + lastEnd,
                        globalOffset + sentenceEnd,
                        true  // 段落内句子
                    ));
                }

                lastEnd = sentenceEnd;
            }

            // 处理段落末尾没有标点的内容
            if (lastEnd < paragraph.length()) {
                String remaining = paragraph.substring(lastEnd).trim();
                if (!remaining.isEmpty()) {
                    sentences.add(new Sentence(
                        remaining,
                        globalOffset + lastEnd,
                        globalOffset + paragraph.length(),
                        true
                    ));
                }
            }

            // 标记段落最后一个句子
            if (!sentences.isEmpty()) {
                Sentence lastSentence = sentences.getLast();
                lastSentence.isParagraphEnd = true;
            }

            globalOffset += paragraph.length() + 2;
        }

        return sentences;
    }

    /**
     * 检查是否为话题转换标记词
     */
    private boolean isTopicShiftMarker(String text) {
        String lower = text.toLowerCase();
        for (String marker : TOPIC_SHIFT_MARKERS) {
            if (lower.startsWith(marker)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 保存分块
     */
    private void saveChunk(List<Chunk> chunks, String content, String docId, String docName,
                          int startOffset, int endOffset, int index,
                          List<String> sectionPath, ChunkingOptions options) {
        String trimmed = content.trim();
        if (trimmed.isEmpty()) return;

        Chunk chunk = Chunk.builder()
            .chunkIndex(index)
            .content(trimmed)
            .contentLength(trimmed.length())
            .documentId(docId)
            .documentName(docName)
            .startOffset(startOffset)
            .endOffset(endOffset)
            .sectionPath(sectionPath)
            .chunkType(detectChunkType(trimmed))
            .startsWithCompleteSentence(isCompleteSentenceStart(trimmed))
            .endsWithCompleteSentence(isCompleteSentenceEnd(trimmed))
            .chunkingStrategy(STRATEGY_NAME)
            .build();

        // 提取关键词（简单实现：取前几个实词）
        if (options.isExtractKeywords()) {
            chunk.setKeywords(extractKeywords(trimmed, options.getKeywordCount()));
        }

        chunks.add(chunk);
    }

    /**
     * 提取关键词（简单实现）
     */
    private List<String> extractKeywords(String text, int count) {
        // 分词并统计词频（简化版）
        String[] words = text.split("\\s+|[^\\w\\u4e00-\\u9fa5]+");
        Map<String, Integer> freq = new HashMap<>();

        for (String word : words) {
            word = word.trim().toLowerCase();
            if (word.length() > 1 && !isStopWord(word)) {
                freq.merge(word, 1, Integer::sum);
            }
        }

        return freq.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(count)
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * 停用词检查
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
            "the", "a", "an", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "will",
            "would", "could", "should", "may", "might", "must", "shall",
            "can", "need", "dare", "ought", "used", "的", "了", "在",
            "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个",
            "上", "也", "很", "到", "说", "要", "去", "你", "会", "着",
            "没有", "看", "好", "自己", "这", "那", "这些", "那些"
        );
        return stopWords.contains(word);
    }

    private boolean isHeading(String text) {
        String firstLine = text.split("\n")[0].trim();
        return firstLine.startsWith("#") ||
               firstLine.matches("^\\d+[\\.\\)\\s]+") ||
               firstLine.matches("^第[一二三四五六七八九十\\d]+[章节篇]");
    }

    private String extractHeading(String text) {
        String firstLine = text.split("\n")[0].trim();
        return firstLine.replaceAll("^#+\\s*", "")
                       .replaceAll("^\\d+[\\.\\)\\s]*", "");
    }

    private int getHeadingLevel(String text) {
        String firstLine = text.split("\n")[0].trim();
        if (firstLine.startsWith("#")) {
            int level = 0;
            for (char c : firstLine.toCharArray()) {
                if (c == '#') level++;
                else break;
            }
            return level;
        }
        return 1;
    }

    /**
     * 句子内部类
     */
    private static class Sentence {
        final String text;
        final int startOffset;
        final int endOffset;
        boolean isParagraphEnd;

        Sentence(String text, int startOffset, int endOffset, boolean isParagraphEnd) {
            this.text = text;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.isParagraphEnd = isParagraphEnd;
        }
    }
}
