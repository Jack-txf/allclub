package com.feng.rag.model.rerank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Rerank 响应结果
 *
 * @author txf
 * @since 2026/3/31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RerankResponse {

    private String errorMsg;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * Rerank 结果列表（按相关性排序）
     */
    private List<RerankResult> results;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return errorMsg == null || errorMsg.isEmpty();
    }
}
