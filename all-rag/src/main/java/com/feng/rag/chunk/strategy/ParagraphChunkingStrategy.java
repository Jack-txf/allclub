package com.feng.rag.chunk.strategy;

import com.feng.rag.chunk.model.Chunk;
import com.feng.rag.chunk.model.ChunkingOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 段落分块策略 —— 以段落为单位，智能合并小段落
 *
 * <p>特点：
 * <ul>
 *   <li>保持段落完整性，不切断句子</li>
 *   <li>相邻小段落智能合并</li>
 *   <li>适合文章、报告等结构化文本</li>
 * </ul>
 *
 * <p>适用场景：
 * <ul>
 *   <li>新闻报道、博客文章</li>
 *   <li>学术论文、技术文档</li>
 *   <li>法律文件、合同文本</li>
 * </ul>
 */
@Slf4j
@Component
public class ParagraphChunkingStrategy extends AbstractChunkingStrategy {

    public static final String STRATEGY_NAME = "paragraph";

    // 匹配段落：连续两个或多个换行符，允许中间有空格
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("(\r?\n\\s*){2,}");
    // 匹配句子：中文或英文结束标点
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("([^。！？.!?]+[。！？.!?]?)");

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public String getStrategyDescription() {
        return "段落分块策略：以段落为单位进行分块，保持段落边界完整。" +
               "相邻小段落会智能合并，大段落会按句子边界拆分。" +
               "适合文章、报告等结构化文本。";
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
            .build();
    }

    @Override
    protected List<Chunk> doChunk(String content, String docId, String docName, ChunkingOptions options) {
        List<Chunk> chunks = new ArrayList<>();
        if (content == null || content.isEmpty()) return chunks;
        int lastMatchEnd = 0;
        int chunkIndex = 0;
        StringBuilder currentBuffer = new StringBuilder();
        int bufferStartOffset = 0;

        Matcher matcher = PARAGRAPH_PATTERN.matcher(content);
        while (true) {
            String paragraph;
            int paraStart, paraEnd;
            boolean isLast = !matcher.find();

            if (isLast) {
                paragraph = content.substring(lastMatchEnd);
                paraStart = lastMatchEnd;
                paraEnd = content.length();
            } else {
                paragraph = content.substring(lastMatchEnd, matcher.start());
                paraStart = lastMatchEnd;
                paraEnd = matcher.start();
                lastMatchEnd = matcher.end(); // 指向下一个段落的开始
            }

            if (!paragraph.trim().isEmpty()) {
                // 如果单段过大，需深层切分
                if (paragraph.length() > options.getMaxChunkSize()) {
                    // 先清空缓冲区
                    if (!currentBuffer.isEmpty()) {
                        saveChunk(chunks, currentBuffer.toString(), docId, docName, bufferStartOffset, paraStart, chunkIndex++, options);
                        currentBuffer.setLength(0);
                    }
                    // 处理大段落
                    chunkIndex = handleLargeParagraph(chunks, paragraph, paraStart, docId, docName, chunkIndex, options);
                    bufferStartOffset = paraEnd;
                } else {
                    // 检查合并后是否超过目标大小
                    int potentialSize = currentBuffer.length() + (!currentBuffer.isEmpty() ? 2 : 0) + paragraph.length();

                    if (potentialSize > options.getTargetChunkSize() && currentBuffer.length() >= options.getMinChunkSize()) {
                        saveChunk(chunks, currentBuffer.toString(), docId, docName, bufferStartOffset, paraStart, chunkIndex++, options);
                        currentBuffer.setLength(0);
                        bufferStartOffset = paraStart;
                    }
                    if (!currentBuffer.isEmpty()) currentBuffer.append("\n\n");
                    else bufferStartOffset = paraStart;

                    currentBuffer.append(paragraph);
                }
            }
            if (isLast) break;
        }
        // 剩余内容
        if (!currentBuffer.isEmpty()) {
            saveChunk(chunks, currentBuffer.toString(), docId, docName, bufferStartOffset, content.length(), chunkIndex, options);
        }
        return chunks;
    }

    /**
     * 递归/深层切分超大段落
     */
    private int handleLargeParagraph(List<Chunk> chunks, String paragraph, int offsetBase, String docId, String docName, int index, ChunkingOptions options) {
        Matcher matcher = SENTENCE_PATTERN.matcher(paragraph);
        StringBuilder sentenceBuffer = new StringBuilder();
        int sentenceStartInPara = 0;
        int currentIdx = index;

        while (matcher.find()) {
            String sentence = matcher.group();
            int sentStart = matcher.start();

            // 如果句子本身就超过 MaxSize，直接强切字符
            if (sentence.length() > options.getMaxChunkSize()) {
                if (!sentenceBuffer.isEmpty()) {
                    saveChunk(chunks, sentenceBuffer.toString(), docId, docName, offsetBase + sentenceStartInPara, offsetBase + sentStart, currentIdx++, options);
                    sentenceBuffer.setLength(0);
                }
                currentIdx = forceSplitString(chunks, sentence, offsetBase + sentStart, docId, docName, currentIdx, options);
                sentenceStartInPara = matcher.end();
            }
            else if (sentenceBuffer.length() + sentence.length() > options.getTargetChunkSize()) {
                saveChunk(chunks, sentenceBuffer.toString(), docId, docName, offsetBase + sentenceStartInPara, offsetBase + sentStart, currentIdx++, options);
                sentenceBuffer = new StringBuilder(sentence);
                sentenceStartInPara = sentStart;
            } else {
                sentenceBuffer.append(sentence);
            }
        }

        if (!sentenceBuffer.isEmpty()) {
            saveChunk(chunks, sentenceBuffer.toString(), docId, docName, offsetBase + sentenceStartInPara, offsetBase + paragraph.length(), currentIdx++, options);
        }
        return currentIdx;
    }

    /**
     * 终极兜底：按字符数强切
     */
    private int forceSplitString(List<Chunk> chunks, String text, int offsetBase, String docId, String docName, int index, ChunkingOptions options) {
        int start = 0;
        int currentIdx = index;
        while (start < text.length()) {
            int end = Math.min(start + options.getTargetChunkSize(), text.length());
            // 简单处理：避免在切分处出现 Unicode 高低代理对 (Optional)
            if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))) {
                end--;
            }
            saveChunk(chunks, text.substring(start, end), docId, docName, offsetBase + start, offsetBase + end, currentIdx++, options);
            start = end;
        }
        return currentIdx;
    }

    private void saveChunk(List<Chunk> chunks, String content, String docId, String docName, int start, int end, int index, ChunkingOptions options) {
        String trimmed = content.trim();
        if (trimmed.isEmpty()) return;

        chunks.add(Chunk.builder()
                .chunkIndex(index)
                .content(trimmed)
                .contentLength(trimmed.length())
                .documentId(docId)
                .documentName(docName)
                .startOffset(start)
                .endOffset(end)
                .chunkingStrategy("PRO_PARAGRAPH_V1")
                .build());
    }
}
