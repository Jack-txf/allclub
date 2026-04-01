package com.feng.rag.retrieval.obj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话轮次 - 存储一轮对话（用户提问 + AI回答）
 *
 * @author txf
 * @since 2026/3/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueTurn {

    /**
     * 用户问题（原始输入）
     */
    private String userQuery;

    /**
     * AI回答
     */
    private String aiResponse;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 对话轮次序号
     */
    private Integer turnNumber;

    public DialogueTurn(String userQuery, String aiResponse) {
        this.userQuery = userQuery;
        this.aiResponse = aiResponse;
        this.timestamp = LocalDateTime.now();
    }
}
