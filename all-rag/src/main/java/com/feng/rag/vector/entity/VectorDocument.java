package com.feng.rag.vector.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量文档实体
 *
 * @author txf
 * @since 2026/3/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorDocument {

    /**
     * 文档唯一 ID
     */
    private String doc_id;

    /**
     * 文档内容（原始文本）
     */
    private String content;

    /**
     * 向量数据
     */
    private List<Float> vector;

    /**
     * 文档来源（如文件名、URL等）
     */
    // private String source;

    /**
     * 分块索引（如果是分块后的内容）
     */
    private Integer chunkIndex;

    /**
     * 元数据
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    /**
     * 创建时间
     */
    private Instant createTime;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档类型
     */
    private String docType;

    /**
     * 获取元数据值
     */
    public String getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * 添加元数据
     */
    public VectorDocument addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
}
