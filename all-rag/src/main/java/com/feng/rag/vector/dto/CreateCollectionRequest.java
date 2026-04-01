package com.feng.rag.vector.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建集合请求
 *
 * @author txf
 * @since 2026/3/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollectionRequest {

    /**
     * 集合名称（可选，不传使用默认配置）
     */
    private String collectionName;

    /**
     * 向量维度（可选，不传使用默认配置）
     */
    @Min(value = 1, message = "维度必须大于0")
    private Integer dimension = 256;
}
