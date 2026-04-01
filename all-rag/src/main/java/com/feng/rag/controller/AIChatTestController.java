package com.feng.rag.controller;

import com.feng.rag.model.AbstractModel;
import com.feng.rag.model.ModelFactory;
import com.feng.rag.model.siliconflow.SiliconflowModel;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @Description:
 * @Author: txf
 * @Date: 2026/3/24
 */
@RestController
@RequestMapping("/ai-chat-test")
public class AIChatTestController {
    @Resource
    private ModelFactory modelFactory;

    @GetMapping("/chatSync")
    public R chatSync(@RequestParam("message") String message) {
        return modelFactory.getModel(SiliconflowModel.SILICONFLOW)
                .chatSync(List.of(new AbstractModel.Message("user", message)));
    }

    // 流式对话 - 返回 SseEmitter
    @GetMapping( path = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam("message") String message) {
        return modelFactory.getModel(SiliconflowModel.SILICONFLOW)
                .chatStream(List.of(new AbstractModel.Message("user", message)));
    }
}
