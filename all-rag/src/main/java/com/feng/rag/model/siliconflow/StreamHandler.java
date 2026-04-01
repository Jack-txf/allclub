package com.feng.rag.model.siliconflow;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * @Description: 流式处理
 * @Author: txf
 * @Date: 2026/3/25
 */
@Slf4j
public class StreamHandler extends EventSourceListener {
    private final SseEmitter emitter;
    public StreamHandler(SseEmitter emitter) {
        this.emitter = emitter;
    }

    // TODO：消息拼接 + 数据库存储

    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        super.onOpen(eventSource, response);
    }

    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
        log.info("data: {}", data);
        if (data.equals("[DONE]")) {
            emitter.complete();
        } else {
            try {
                emitter.send(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        super.onClosed(eventSource);
    }

    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        super.onFailure(eventSource, t, response);
    }
}
