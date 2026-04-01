package com.feng.rag.datasource.util;

import java.util.Arrays;
import java.util.Map;

/**
 * Chunk 质量评分器
 *
 * 在 Chunking 之后、Embedding 之前执行。
 * 每个 chunk 独立评分，低于阈值则丢弃。
 */
public class ChunkQualityScorer {

    /**
     * 评分结果
     */
    public record QualityScore(
        double score,           // 综合分 0.0 ~ 1.0
        boolean pass,           // 是否通过（score > threshold）
        String rejectReason     // 拒绝原因（pass=false 时有值）
    ) {}

    /**
     * 对单个 chunk 评分
     *
     * @param chunk chunk 文本
     * @param threshold 通过阈值（建议 0.5）
     */
    public QualityScore score(String chunk, double threshold) {
        if (chunk == null || chunk.isBlank()) {
            return new QualityScore(0.0, false, "空内容");
        }

        // ── 规则1：长度过滤 ──────────────────────────────────────
        // 太短（<30字）：没有检索价值；太长（>5000字）：语义太分散（说明切分有问题）
        int length = chunk.length();
        if (length < 30) {
            return new QualityScore(0.0, false,
                "文本过短（" + length + "字 < 30字最低要求）");
        }
        if (length > 5000) {
            return new QualityScore(0.0, false,
                "文本过长（" + length + "字 > 5000字，切分策略可能异常）");
        }

        // ── 规则2：有效字符比例 ───────────────────────────────────
        // 计算中文字符 + 拉丁字母 + 数字 + 常用标点的比例
        // 比例过低说明是乱码或特殊字符密集区
        double validRatio = calcValidCharRatio(chunk);
        if (validRatio < 0.55) {
            return new QualityScore(validRatio, false,
                String.format("有效字符比例过低（%.1f%% < 55%%），可能含大量乱码", validRatio * 100));
        }

        // ── 规则3：重复内容检测 ───────────────────────────────────
        // 同一行/句子出现次数超过阈值，说明是表头重复或水印
        double repeatRatio = calcRepeatLineRatio(chunk);
        if (repeatRatio > 0.4) {
            return new QualityScore(1 - repeatRatio, false,
                String.format("重复行比例过高（%.1f%% > 40%%），可能是重复页眉或表格", repeatRatio * 100));
        }

        // ── 规则4：信息密度估算 ───────────────────────────────────
        // 独特词汇数 / 总词数，低于阈值说明内容冗余（大量重复词）
        double uniqueTokenRatio = calcUniqueTokenRatio(chunk);
        if (uniqueTokenRatio < 0.2) {
            return new QualityScore(uniqueTokenRatio, false,
                "信息密度过低（独特词汇比例 < 20%），内容高度重复");
        }

        // ── 综合评分 ─────────────────────────────────────────────
        // 加权平均：有效字符比例权重最高，独特词汇次之
        double compositeScore =
            validRatio * 0.4 +
            uniqueTokenRatio * 0.4 +
            (1.0 - repeatRatio) * 0.2;

        return new QualityScore(compositeScore, compositeScore >= threshold, null);
    }

    private double calcValidCharRatio(String text) {
        long valid = text.chars().filter(c ->
            (c >= 0x4E00 && c <= 0x9FFF)  // 中文
            || Character.isLetterOrDigit(c) // 字母数字
            || "，。！？、；：,.!?;: \n\t".indexOf(c) >= 0 // 常用标点和空白
        ).count();
        return (double) valid / text.length();
    }

    private double calcRepeatLineRatio(String text) {
        String[] lines = text.split("\n");
        if (lines.length < 3) return 0.0;

        Map<String, Long> counts = Arrays.stream(lines)
            .map(String::strip)
            .filter(l -> !l.isEmpty())
            .collect(java.util.stream.Collectors.groupingBy(
                l -> l, java.util.stream.Collectors.counting()
            ));

        // 出现超过2次的行的数量 / 总行数
        long repeatedLines = counts.values().stream().filter(c -> c > 2).count();
        return (double) repeatedLines / lines.length;
    }

    private double calcUniqueTokenRatio(String text) {
        // 简单按空格和标点切词
        String[] tokens = text.split("[\\s，。！？、；：,.!?;:\\-]+");
        if (tokens.length == 0) return 0.0;

        long uniqueCount = Arrays.stream(tokens)
            .filter(t -> !t.isBlank())
            .distinct()
            .count();
        return (double) uniqueCount / tokens.length;
    }
}