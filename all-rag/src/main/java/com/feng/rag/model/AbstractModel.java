package com.feng.rag.model;

import com.feng.rag.controller.R;
import com.feng.rag.model.embedding.EmbeddingResponse;
import com.feng.rag.model.rerank.RerankResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @Description:
 * @Author: txf
 * @Date: 2026/3/24
 */
public abstract class AbstractModel {

    //----------------------------------- 对话部分 -----------------------------------
    // 方法一--同步chat测试
    public abstract R chatSync(List<Message> messages);

    // 方法二-- 流式chat测试
    public abstract SseEmitter chatStream(List<Message> messages);

    public record Message(String role, String content) {
    }

    //----------------------------------- Embedding部分 -----------------------------------
    public abstract EmbeddingResponse embedding(List<String> text);

    //----------------------------------- Rerank部分 -----------------------------------
    /**
     * 重排序（Rerank）文档列表
     *
     * @param query     查询文本
     * @param documents 待排序的文档列表
     * @param topN      返回前N个最相关的结果（null表示返回所有）
     * @return RerankResponse 包含按相关性排序的结果
     */
    public abstract RerankResponse rerank(String query, List<String> documents, Integer topN);

}
