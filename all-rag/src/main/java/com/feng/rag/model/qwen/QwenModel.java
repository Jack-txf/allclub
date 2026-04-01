package com.feng.rag.model.qwen;

import com.feng.rag.controller.R;
import com.feng.rag.model.AbstractModel;
import com.feng.rag.model.config.GlobalModelProperties;
import com.feng.rag.model.embedding.EmbeddingResponse;
import com.feng.rag.model.rerank.RerankResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * @Description: 千问厂商
 * @Author: txf
 * @Date: 2026/3/24
 */
public class QwenModel extends AbstractModel {

    public static final String QWEN = "qwen";

    private GlobalModelProperties.ProviderConfig providerConfig;
    public QwenModel( GlobalModelProperties.ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    @Override
    public R chatSync(List<Message> messages) {
        return R.ok();
    }

    @Override
    public SseEmitter chatStream(List<Message> messages) {
        return null;
    }

    @Override
    public EmbeddingResponse embedding(List<String> text) {
        return null;
    }

    @Override
    public RerankResponse rerank(String query, List<String> documents, Integer topN) {
        // QwenModel 暂未实现 Rerank 功能，直接返回空结果
        return null;
    }
}
