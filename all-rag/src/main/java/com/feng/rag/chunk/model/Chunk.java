package com.feng.rag.chunk.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * 文本分块（Chunk）数据模型 —— RAG 检索的基本单元
 *
 * <p>企业级设计要点：
 * <ul>
 *   <li>完整的溯源信息（来源文档、位置、偏移量）</li>
 *   <li>层级关系支持（父子分块、相邻分块）</li>
 *   <li>丰富的元数据（语义标签、置信度、处理状态）</li>
 *   <li>Embedding 状态追踪（是否已向量化、使用的模型）</li>
 * </ul>
 */
@Data
@Builder
public class Chunk {

    // ==================== 唯一标识 ====================
    /**
     * 分块唯一标识（UUID v7 或 Snowflake ID）
     */
    private String chunkId;
    /**
     * 分块在文档中的序号（从 0 开始）
     */
    private int chunkIndex;

    // ==================== 内容 ====================
    /**
     * 分块的纯文本内容
     */
    private String content;
    /**
     * 内容字符数（content.length()）
     */
    private int contentLength;
    /**
     * 内容 Token 数（由 Tokenizer 计算，用于控制模型输入长度）
     */
    @Builder.Default
    private int tokenCount = 0;
    // ==================== 溯源信息 ====================

    /**
     * 来源文档 ID（关联 DocumentParseResult）
     */
    private String documentId;
    /**
     * 来源文档名称
     */
    private String documentName;
    /**
     * 在原文档中的起始字符位置（绝对偏移量）
     */
    private int startOffset;
    /**
     * 在原文档中的结束字符位置（不包含）
     */
    private int endOffset;
    /**
     * 所在页码/段落号（多页文档如 PDF、PPT）
     */
    @Builder.Default
    private int pageNumber = 1;

    /**
     * 所在段落序号（文档内部段落编号）
     */
    @Builder.Default
    private int paragraphNumber = 0;

    // ==================== 层级关系 ====================

    /**
     * 父分块 ID（用于递归分块的层级结构）
     * 如：父分块是章节，子分块是段落
     */
    private String parentChunkId;

    /**
     * 子分块 ID 列表
     */
    @Builder.Default
    private List<String> childChunkIds = new ArrayList<>();

    /**
     * 层级深度（0 表示顶层，1 表示子分块，以此类推）
     */
    @Builder.Default
    private int hierarchyLevel = 0;

    /**
     * 前一个分块 ID（用于恢复文档顺序）
     */
    private String prevChunkId;

    /**
     * 后一个分块 ID
     */
    private String nextChunkId;

    // ==================== 语义元数据 ====================

    /**
     * 分块类型（标题、正文、表格、代码块、引用等）
     */
    private ChunkType chunkType;

    /**
     * 语义标签（由分类器自动提取，如 "技术细节"、"背景介绍"、"结论"）
     */
    @Builder.Default
    private List<String> semanticTags = new ArrayList<>();

    /**
     * 章节路径（如 ["第1章", "1.2节", "子节标题"]）
     */
    @Builder.Default
    private List<String> sectionPath = new ArrayList<>();

    /**
     * 关键实体（NER 提取的人名、地名、组织名等）
     */
    @Builder.Default
    private List<String> keyEntities = new ArrayList<>();

    /**
     * 关键词（TF-IDF 或关键词提取算法生成）
     */
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    // ==================== 质量与置信度 ====================

    /**
     * 分块质量评分（0.0 - 1.0）
     * 基于：内容完整性、语义连贯性、信息密度等
     */
    @Builder.Default
    private double qualityScore = 1.0;

    /**
     * 是否以完整句子开始
     */
    @Builder.Default
    private boolean startsWithCompleteSentence = true;

    /**
     * 是否以完整句子结束
     */
    @Builder.Default
    private boolean endsWithCompleteSentence = true;

    /**
     * 是否跨段落（段落边界截断）
     */
    @Builder.Default
    private boolean crossesParagraphBoundary = false;

    /**
     * 处理状态
     */
    @Builder.Default
    private ProcessingStatus status = ProcessingStatus.PENDING;

    // ==================== Embedding 状态 ====================

    /**
     * 是否已向量化
     */
    @Builder.Default
    private boolean embedded = false;

    /**
     * 使用的 Embedding 模型
     */
    private String embeddingModel;

    /**
     * Embedding 向量维度
     */
    private int embeddingDimension;

    /**
     * 向量 ID（在向量数据库中的标识）
     */
    private String vectorId;

    /**
     * Embedding 时间戳
     */
    private Instant embeddedAt;

    // ==================== 扩展元数据 ====================

    /**
     * 自定义扩展属性（业务可自由添加字段）
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    /**
     * 使用的分块策略名称（用于调试和审计）
     */
    private String chunkingStrategy;

    /**
     * 原始内容摘要（用于快速预览，可选）
     */
    private String summary;

    // ==================== 时间戳 ====================

    /**
     * 分块创建时间
     */
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * 最后更新时间
     */
    @Builder.Default
    private Instant updatedAt = Instant.now();

    // ==================== 枚举定义 ====================

    /**
     * 分块类型枚举
     */
    public enum ChunkType {
        /** 文档标题 */
        TITLE,
        /** 章节标题 */
        HEADING,
        /** 正文段落 */
        PARAGRAPH,
        /** 列表项 */
        LIST_ITEM,
        /** 表格内容 */
        TABLE,
        /** 代码块 */
        CODE_BLOCK,
        /** 引用/引文 */
        QUOTE,
        /** 图片说明 */
        CAPTION,
        /** 页眉页脚 */
        HEADER_FOOTER,
        /** 其他 */
        OTHER
    }

    /**
     * 处理状态枚举
     */
    public enum ProcessingStatus {
        /** 等待处理 */
        PENDING,
        /** 处理中 */
        PROCESSING,
        /** 处理完成 */
        COMPLETED,
        /** 处理失败 */
        FAILED,
        /** 已废弃（质量太差被过滤） */
        DISCARDED
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取带上下文的预览文本（用于搜索结果展示）
     */
    public String getPreview(int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * 获取章节路径字符串（如 "第1章 > 1.2节 > 子节"）
     */
    public String getSectionPathString() {
        return String.join(" > ", sectionPath);
    }

    /**
     * 检查是否为有效分块（内容非空、质量达标）
     */
    public boolean isValid() {
        return content != null && !content.isBlank()
            && contentLength > 0
            && qualityScore >= 0.3  // 最低质量阈值
            && status != ProcessingStatus.DISCARDED;
    }

    /**
     * 获取分块上下文信息（用于 RAG prompt 增强）
     */
    public ChunkContext getContext() {
        return ChunkContext.builder()
            .documentName(documentName)
            .sectionPath(sectionPath)
            .pageNumber(pageNumber)
            .paragraphNumber(paragraphNumber)
            .prevChunkId(prevChunkId)
            .nextChunkId(nextChunkId)
            .build();
    }

    // ==================== 内部类：上下文信息 ====================

    /**
     * 分块上下文（轻量级，用于 RAG 检索时增强 prompt）
     */
    @Data
    @Builder
    public static class ChunkContext {
        private String documentName;
        private List<String> sectionPath;
        private int pageNumber;
        private int paragraphNumber;
        private String prevChunkId;
        private String nextChunkId;
    }
}
