package com.feng.rag.controller;

import com.feng.rag.retrieval.RetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 在线检索 Controller - 处理用户查询的在线RAG流程
 *
 * @author txf
 * @since 2026/3/29
 */
@Slf4j
@RestController
@RequestMapping("/v1/retrieval")
@RequiredArgsConstructor
public class RetrievalController {

    private final RetrievalService retrievalService;

    /**
     * 处理用户查询（完整流程入口）
     *
     * @param request 请求体（包含query和sessionId）
     * @return 处理结果
     */
    @PostMapping("/query")
    public R processQuery(@RequestBody QueryRequest request) {
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        log.info("[RetrievalController] 收到查询: sessionId={}, query={}", sessionId, request.query());
        // TODO 后续加上会话ID
        retrievalService.vectorRetrieve(request.query());

        return R.ok();
    }

    /**
     * 向量检索接口
     *
     * @param query 查询文本
     * @param topK  返回结果数量（默认10）
     * @param orgId 组织ID（默认default）
     * @return 检索结果
     */
    @GetMapping("/vector-search")
    public R vectorSearch(@RequestParam String query,
                          @RequestParam(required = false, defaultValue = "10") Integer topK,
                          @RequestParam(required = false, defaultValue = "default") String orgId) {
        log.info("[RetrievalController] 向量检索: query={}, topK={}, orgId={}", query, topK, orgId);

        retrievalService.vectorRetrieve(query);

        return R.ok();
    }

    /**
     * 稀疏检索接口（关键词匹配）
     *
     * @param query 查询文本
     * @param topK  返回结果数量（默认10）
     * @param orgId 组织ID（默认default）
     * @return 检索结果
     */
    @GetMapping("/sparse-search")
    public R sparseSearch(@RequestParam String query,
                          @RequestParam(required = false, defaultValue = "10") Integer topK,
                          @RequestParam(required = false, defaultValue = "default") String orgId) {
        log.info("[RetrievalController] 稀疏检索: query={}, topK={}, orgId={}", query, topK, orgId);

        var results = retrievalService.sparseRetrieve(query);

        return R.ok().add("results", results);
    }

    /**
     * RAG问答接口（完整流程：检索+重排+生成回答）
     *
     * @param request 请求体（包含query、sessionId等）
     * @return AI回答
     */
    @PostMapping("/rag-answer")
    public R ragAnswer(@RequestBody RagAnswerRequest request) {
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        log.info("[RetrievalController] RAG问答请求: sessionId={}, query={}",
                sessionId, request.query());

        R response = retrievalService.ragAnswer(
                request.query(),
                request.topK(),
                request.topN(),
                request.orgId(),
                sessionId
        );

        return response.add("sessionId", sessionId);
    }

    /**
     * 简化版RAG问答（使用默认参数）
     */
    @PostMapping("/rag-answer-simple")
    public R ragAnswerSimple(@RequestBody QueryRequest request) {
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        log.info("[RetrievalController] 简化RAG问答请求: sessionId={}, query={}",
                sessionId, request.query());

        R response = retrievalService.ragAnswer(request.query());
        return response.add("sessionId", sessionId);
    }

    /**
     * 请求体
     */
    public record QueryRequest(String query, String sessionId) {
    }

    /**
     * RAG问答请求体（完整参数）
     */
    public record RagAnswerRequest(String query, String sessionId, Integer topK, Integer topN, String orgId) {
    }
}
