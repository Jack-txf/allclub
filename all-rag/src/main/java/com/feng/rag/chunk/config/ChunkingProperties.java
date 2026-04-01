package com.feng.rag.chunk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 分块模块配置属性
 *
 * <p>支持全局默认配置 + 按文档类型的差异化配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rag.chunk")
public class ChunkingProperties {

    /**
     * 默认分块策略
     */
    // 分块策略: fixed_size | paragraph | recursive | semantic | sliding_window
    private String defaultStrategy = "sliding_window";

    /**
     * 是否启用分块
     */
    private boolean enabled = true;

    /**
     * 全局默认配置
     */
    private ChunkConfig defaults = new ChunkConfig();

    /**
     * 按文档类型的差异化配置（key: MIME type 或扩展名）
     */
    private Map<String, ChunkConfig> typeOverrides = new HashMap<>();

    /**
     * 分块配置项
     */
    @Data
    public static class ChunkConfig {
        /**
         * 分块策略名称
         */
        private String strategy;

        /**
         * 目标分块大小（字符数）
         */
        private Integer targetChunkSize;

        /**
         * 最小分块大小
         */
        private Integer minChunkSize;

        /**
         * 最大分块大小
         */
        private Integer maxChunkSize;

        /**
         * 重叠大小
         */
        private Integer overlapSize;

        /**
         * 是否保留段落边界
         */
        private Boolean respectParagraphBoundaries;

        /**
         * 是否保留句子边界
         */
        private Boolean respectSentenceBoundaries;

        /**
         * 是否启用质量过滤
         */
        private Boolean enableQualityFilter;

        /**
         * 最低质量分
         */
        private Double minQualityScore;

        /**
         * 是否提取关键词
         */
        private Boolean extractKeywords;
    }

    /**
     * 获取指定类型的配置（合并默认值和覆盖值）
     */
    public ChunkConfig getConfigForType(String mimeType, String fileExtension) {
        ChunkConfig config = new ChunkConfig();

        // 先应用默认值
        if (defaults != null) {
            copyConfig(defaults, config);
        }

        // 再应用类型覆盖
        ChunkConfig override = null;
        if (mimeType != null && typeOverrides.containsKey(mimeType)) {
            override = typeOverrides.get(mimeType);
        } else if (fileExtension != null && typeOverrides.containsKey(fileExtension.toLowerCase())) {
            override = typeOverrides.get(fileExtension.toLowerCase());
        }

        if (override != null) {
            copyConfig(override, config);
        }

        return config;
    }

    private void copyConfig(ChunkConfig source, ChunkConfig target) {
        if (source.getStrategy() != null) target.setStrategy(source.getStrategy());
        if (source.getTargetChunkSize() != null) target.setTargetChunkSize(source.getTargetChunkSize());
        if (source.getMinChunkSize() != null) target.setMinChunkSize(source.getMinChunkSize());
        if (source.getMaxChunkSize() != null) target.setMaxChunkSize(source.getMaxChunkSize());
        if (source.getOverlapSize() != null) target.setOverlapSize(source.getOverlapSize());
        if (source.getRespectParagraphBoundaries() != null) target.setRespectParagraphBoundaries(source.getRespectParagraphBoundaries());
        if (source.getRespectSentenceBoundaries() != null) target.setRespectSentenceBoundaries(source.getRespectSentenceBoundaries());
        if (source.getEnableQualityFilter() != null) target.setEnableQualityFilter(source.getEnableQualityFilter());
        if (source.getMinQualityScore() != null) target.setMinQualityScore(source.getMinQualityScore());
        if (source.getExtractKeywords() != null) target.setExtractKeywords(source.getExtractKeywords());
    }
}
