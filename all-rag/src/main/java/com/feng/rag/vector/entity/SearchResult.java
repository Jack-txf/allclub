package com.feng.rag.vector.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 向量搜索结果
 *
 * @author txf
 * @since 2026/3/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    /**
     * 文档ID
     */
    private String docId;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 相似度分数
     */
    private Float score;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 分块索引
     */
    private Integer chunkIndex;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 距离（Milvus返回的原始距离值）
     */
    private Float distance;
}
