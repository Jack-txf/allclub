package com.feng.rag.model.rerank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rerank 结果项
 *
 * @author txf
 * @since 2026/3/31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RerankResult {

    /**
     * 文档在原始输入中的索引
     */
    private Integer index;

    /**
     * 相关性分数（0-1之间，越高越相关）
     */
    private Double relevanceScore;

    /**
     * 原始文档内容
     */
    private String document;
}
