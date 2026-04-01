package com.feng.rag.retrieval.obj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户输入处理结果
 *
 * @author txf
 * @since 2026/3/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedQuery {

    /**
     * 原始用户输入
     */
    private String originalQuery;

    /**
     * 识别到的用户意图
     */
    private UserIntent intent;

    /**
     * 重写后的查询（独立、完整、适合检索）
     * 如果是闲聊或敏感词，此字段可能为空
     */
    private String rewrittenQuery;

    /**
     * 意图识别理由（用于调试）
     */
    private String intentReason;

    /**
     * 是否需要走检索流程
     */
    public boolean needsRetrieval() {
        return intent != null && intent.needsRetrieval();
    }

    /**
     * 是否直接拒绝回答
     */
    public boolean shouldReject() {
        return intent != null && intent.shouldReject();
    }

    /**
     * 是否直接走LLM闲聊
     */
    public boolean isChitchat() {
        return intent == UserIntent.CHITCHAT;
    }
}
