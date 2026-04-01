package com.feng.rag.retrieval.obj;

import lombok.Getter;

/**
 * 用户意图枚举
 *
 * @author txf
 * @since 2026/3/29
 */
@Getter
public enum UserIntent {
    /**
     * 知识问答 - 需要进行检索
     */
    KNOWLEDGE_QUERY("knowledge_query", "知识问答"),

    /**
     * 闲聊 - 直接走LLM，不检索
     */
    CHITCHAT("chitchat", "闲聊"),

    /**
     * 敏感词 - 需要拦截
     */
    SENSITIVE("sensitive", "敏感词"),

    /**
     * 未知/其他
     */
    UNKNOWN("unknown", "未知");

    private final String code;
    private final String description;

    UserIntent(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 是否需要走检索流程
     */
    public boolean needsRetrieval() {
        return this == KNOWLEDGE_QUERY;
    }

    /**
     * 是否直接拒绝
     */
    public boolean shouldReject() {
        return this == SENSITIVE;
    }

    public static UserIntent fromCode(String code) {
        for (UserIntent intent : values()) {
            if (intent.code.equalsIgnoreCase(code)) {
                return intent;
            }
        }
        return UNKNOWN;
    }
}