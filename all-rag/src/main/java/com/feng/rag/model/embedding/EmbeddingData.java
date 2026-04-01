package com.feng.rag.model.embedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Embedding 数据项
 *
 * @author txf
 * @since 2026/3/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingData {

    /**
     * 对象类型，通常为 "embedding"
     */
    private String object;

    /**
     * Embedding 向量（浮点数数组）
     */
    private List<Float> embedding;

    /**
     * 数据项索引
     */
    private Integer index;
}
