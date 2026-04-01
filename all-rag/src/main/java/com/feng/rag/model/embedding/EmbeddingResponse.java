package com.feng.rag.model.embedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Embedding 响应结果
 *
 * @author txf
 * @since 2026/3/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {

    private String errorMsg;

    /**
     * 对象类型，通常为 "list"
     */
    private String object;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * Embedding 数据列表
     */
    private List<EmbeddingData> data;

    /**
     * Token 使用情况
     */
    private EmbeddingUsage usage;
}
