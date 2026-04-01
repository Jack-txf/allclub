package com.feng.rag.vector.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 向量搜索请求
 *
 * @author txf
 * @since 2026/3/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    /**
     * 查询向量
     */
    @NotEmpty(message = "查询向量不能为空")
    private List<Float> vector;

    /**
     * 返回结果数量
     */
    @NotNull(message = "topK不能为空")
    @Min(value = 1, message = "topK至少为1")
    @Max(value = 50, message = "topK最大为50")
    private Integer topK;

    /**
     * 过滤表达式（可选）
     * 例如: "source == 'doc1.pdf'"
     */
    private String filterExpr;
}
