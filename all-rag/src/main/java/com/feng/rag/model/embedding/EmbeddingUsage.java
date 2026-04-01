package com.feng.rag.model.embedding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedding Token 使用情况
 *
 * @author txf
 * @since 2026/3/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmbeddingUsage {

    /**
     * 提示词 Token 数量
     */
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    /**
     * 完成 Token 数量（Embedding 通常为 0）
     */
    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    /**
     * 总 Token 数量
     */
    @JsonProperty("total_tokens")
    private Integer totalTokens;
}
