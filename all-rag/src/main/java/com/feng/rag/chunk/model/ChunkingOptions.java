package com.feng.rag.chunk.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 分块处理选项 —— 控制分块行为的配置参数
 *
 * <p>企业级设计：提供精细化的分块控制，平衡召回率与精确度</p>
 */
@Data
@Builder
public class ChunkingOptions {

    // ==================== 基础分块参数 ====================

    /**
     * 目标分块大小（字符数）
     * 实际分块大小会在 [minChunkSize, maxChunkSize] 范围内
     */
    @Builder.Default
    private int targetChunkSize = 500;

    /**
     * 最小分块大小（字符数）
     * 小于此值的分块将被合并或丢弃
     */
    @Builder.Default
    private int minChunkSize = 50;

    /**
     * 最大分块大小（字符数）
     * 超过此值将强制切分
     */
    @Builder.Default
    private int maxChunkSize = 1000;

    /**
     * 分块重叠大小（字符数）
     * 用于保持上下文连续性，推荐为 chunkSize 的 10%-20%
     */
    @Builder.Default
    private int overlapSize = 50;

    // ==================== 语义边界参数 ====================

    /**
     * 是否优先在段落边界切分
     */
    @Builder.Default
    private boolean respectParagraphBoundaries = true;

    /**
     * 是否优先在句子边界切分
     */
    @Builder.Default
    private boolean respectSentenceBoundaries = true;

    /**
     * 是否在标题/章节边界强制切分
     */
    @Builder.Default
    private boolean splitAtHeadings = true;

    /**
     * 章节标题标记正则（如 "^#{1,6}\\s"、"^\\d+\\."）
     */
    @Builder.Default
    private String headingPattern = "^#{1,6}\\s|^\\d+[\\.\\)\\s]|^第[一二三四五六七八九十\\d]+[章节篇]";

    // ==================== 特殊内容处理 ====================

    /**
     * 是否保留代码块完整性（不在代码块中间切分）
     */
    @Builder.Default
    private boolean preserveCodeBlocks = true;

    /**
     * 代码块标记（如 ```）
     */
    @Builder.Default
    private String codeBlockMarker = "```";

    /**
     * 是否保留表格完整性
     */
    @Builder.Default
    private boolean preserveTables = true;

    /**
     * 表格分隔符（Markdown 表格用 |）
     */
    @Builder.Default
    private String tableDelimiter = "|";

    /**
     * 是否保留列表项完整性
     */
    @Builder.Default
    private boolean preserveListItems = true;

    /**
     * 列表项标记正则
     */
    @Builder.Default
    private String listItemPattern = "^[-*+•]\\s|^\\d+[\\.\\)]\\s";

    // ==================== 质量过滤参数 ====================

    /**
     * 是否启用质量过滤
     */
    @Builder.Default
    private boolean enableQualityFilter = true;

    /**
     * 最低质量评分阈值（0.0-1.0）
     */
    @Builder.Default
    private double minQualityScore = 0.3;

    /**
     * 是否过滤过短分块（小于 minChunkSize）
     */
    @Builder.Default
    private boolean filterShortChunks = true;

    /**
     * 是否过滤重复内容
     */
    @Builder.Default
    private boolean filterDuplicates = true;

    /**
     * 重复内容相似度阈值（0.0-1.0）
     */
    @Builder.Default
    private double duplicateThreshold = 0.95;

    // ==================== 元数据增强参数 ====================

    /**
     * 是否提取关键词
     */
    @Builder.Default
    private boolean extractKeywords = false;

    /**
     * 关键词数量
     */
    @Builder.Default
    private int keywordCount = 5;

    /**
     * 是否生成摘要
     */
    @Builder.Default
    private boolean generateSummary = false;

    /**
     * 摘要最大长度
     */
    @Builder.Default
    private int summaryMaxLength = 100;

    /**
     * 是否识别分块类型
     */
    @Builder.Default
    private boolean detectChunkType = true;

    // ==================== 高级参数 ====================

    /**
     * 递归分块的最大层级深度（0 表示不限制）
     */
    @Builder.Default
    private int maxHierarchyDepth = 3;

    /**
     * 语义分块的相似度阈值（用于语义聚类）
     */
    @Builder.Default
    private double semanticSimilarityThreshold = 0.8;

    /**
     * 是否启用动态分块大小（根据内容复杂度调整）
     */
    @Builder.Default
    private boolean enableDynamicSizing = false;

    /**
     * 动态大小的最小 Token 数（用于 Embedding 模型限制）
     */
    @Builder.Default
    private int minTokenCount = 50;

    /**
     * 动态大小的最大 Token 数
     */
    @Builder.Default
    private int maxTokenCount = 512;

    // ==================== 扩展参数 ====================

    /**
     * 自定义扩展参数（供特定策略使用）
     */
    @Builder.Default
    private Map<String, Object> extraParams = new HashMap<>();

    // ==================== 预设配置工厂方法 ====================

    /**
     * 默认配置（平衡型）
     */
    public static ChunkingOptions defaultOptions() {
        return ChunkingOptions.builder().build();
    }

    /**
     * 高精度检索配置（小分块，高重叠）
     * 适用于：需要精确匹配的场景，如法律条文、技术规范
     */
    public static ChunkingOptions highPrecision() {
        return ChunkingOptions.builder()
            .targetChunkSize(300)
            .minChunkSize(50)
            .maxChunkSize(500)
            .overlapSize(100)
            .respectParagraphBoundaries(true)
            .respectSentenceBoundaries(true)
            .minQualityScore(0.5)
            .build();
    }

    /**
     * 高召回率配置（大分块，低重叠）
     * 适用于：需要全面理解的场景，如论文摘要、新闻报道
     */
    public static ChunkingOptions highRecall() {
        return ChunkingOptions.builder()
            .targetChunkSize(800)
            .minChunkSize(200)
            .maxChunkSize(1500)
            .overlapSize(50)
            .respectParagraphBoundaries(true)
            .respectSentenceBoundaries(false)
            .generateSummary(true)
            .build();
    }

    /**
     * 问答优化配置（基于问题长度的动态分块）
     * 适用于：FAQ、客服知识库等问答场景
     */
    public static ChunkingOptions qaOptimized() {
        return ChunkingOptions.builder()
            .targetChunkSize(400)
            .minChunkSize(100)
            .maxChunkSize(800)
            .overlapSize(80)
            .respectParagraphBoundaries(true)
            .respectSentenceBoundaries(true)
            .splitAtHeadings(true)
            .extractKeywords(true)
            .keywordCount(3)
            .build();
    }

    /**
     * 代码文档配置（保留代码块和表格）
     * 适用于：技术文档、API 文档、README
     */
    public static ChunkingOptions codeDocumentation() {
        return ChunkingOptions.builder()
            .targetChunkSize(600)
            .minChunkSize(100)
            .maxChunkSize(1200)
            .overlapSize(60)
            .preserveCodeBlocks(true)
            .preserveTables(true)
            .preserveListItems(true)
            .detectChunkType(true)
            .build();
    }
}
