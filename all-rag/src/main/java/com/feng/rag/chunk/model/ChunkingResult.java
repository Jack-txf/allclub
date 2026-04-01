package com.feng.rag.chunk.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分块处理结果 —— 包含分块列表和处理统计
 */
@Data
@Builder
public class ChunkingResult {

    /**
     * 原始文档 ID
     */
    private String documentId;

    /**
     * 原始文档名称
     */
    private String documentName;

    /**
     * 生成的分块列表（按顺序）
     */
    @Builder.Default
    private List<Chunk> chunks = new ArrayList<>();

    /**
     * 使用的分块策略
     */
    private String strategyName;

    /**
     * 使用的分块选项
     */
    private ChunkingOptions options;

    /**
     * 处理状态
     */
    @Builder.Default
    private Status status = Status.SUCCESS;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    // ==================== 统计信息 ====================

    /**
     * 原始文本字符数
     */
    private int originalTextLength;

    /**
     * 生成的分块总数
     */
    public int getTotalChunks() {
        return chunks.size();
    }

    /**
     * 有效分块数（未废弃）
     */
    public int getValidChunks() {
        return (int) chunks.stream().filter(Chunk::isValid).count();
    }

    /**
     * 被过滤的分块数
     */
    public int getFilteredChunks() {
        return getTotalChunks() - getValidChunks();
    }

    /**
     * 总分块内容字符数
     */
    public int getTotalChunkedContentLength() {
        return chunks.stream()
            .mapToInt(Chunk::getContentLength)
            .sum();
    }

    /**
     * 平均分块大小
     */
    public double getAverageChunkSize() {
        if (chunks.isEmpty()) return 0;
        return chunks.stream()
            .mapToInt(Chunk::getContentLength)
            .average()
            .orElse(0);
    }

    /**
     * 平均分块质量评分
     */
    public double getAverageQualityScore() {
        if (chunks.isEmpty()) return 0;
        return chunks.stream()
            .mapToDouble(Chunk::getQualityScore)
            .average()
            .orElse(0);
    }

    /**
     * 获取分块大小分布（用于评估分块质量）
     */
    public Map<String, Long> getSizeDistribution() {
        return chunks.stream().collect(Collectors.groupingBy(
            chunk -> {
                int size = chunk.getContentLength();
                if (size < 100) return "<100";
                if (size < 300) return "100-300";
                if (size < 500) return "300-500";
                if (size < 800) return "500-800";
                if (size < 1200) return "800-1200";
                return ">1200";
            },
            Collectors.counting()
        ));
    }

    /**
     * 获取按类型分组的分块统计
     */
    public Map<Chunk.ChunkType, Long> getTypeDistribution() {
        return chunks.stream()
            .filter(c -> c.getChunkType() != null)
            .collect(Collectors.groupingBy(
                Chunk::getChunkType,
                Collectors.counting()
            ));
    }

    // ==================== 时间戳 ====================

    /**
     * 处理开始时间
     */
    private Instant startTime;

    /**
     * 处理结束时间
     */
    private Instant endTime;

    /**
     * 处理耗时（毫秒）
     */
    public long getDurationMs() {
        if (startTime == null || endTime == null) return 0;
        return endTime.toEpochMilli() - startTime.toEpochMilli();
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取指定索引的分块
     */
    public Chunk getChunk(int index) {
        if (index < 0 || index >= chunks.size()) return null;
        return chunks.get(index);
    }

    /**
     * 获取第一个分块
     */
    public Chunk getFirstChunk() {
        return chunks.isEmpty() ? null : chunks.get(0);
    }

    /**
     * 获取最后一个分块
     */
    public Chunk getLastChunk() {
        return chunks.isEmpty() ? null : chunks.get(chunks.size() - 1);
    }

    /**
     * 获取所有有效分块
     */
    public List<Chunk> getValidChunkList() {
        return chunks.stream()
            .filter(Chunk::isValid)
            .toList();
    }

    /**
     * 按层级获取分块（用于递归分块）
     */
    public List<Chunk> getChunksByLevel(int level) {
        return chunks.stream()
            .filter(c -> c.getHierarchyLevel() == level)
            .toList();
    }

    /**
     * 构建失败结果
     */
    public static ChunkingResult failed(String documentId, String documentName, String errorMessage) {
        return ChunkingResult.builder()
            .documentId(documentId)
            .documentName(documentName)
            .status(Status.FAILED)
            .errorMessage(errorMessage)
            .startTime(Instant.now())
            .endTime(Instant.now())
            .build();
    }

    /**
     * 状态枚举
     */
    public enum Status {
        SUCCESS,
        PARTIAL,  // 部分成功（有分块被过滤）
        FAILED
    }
}
