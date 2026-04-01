package com.feng.rag.retrieval;

import com.feng.rag.controller.R;
import com.feng.rag.model.AbstractModel;
import com.feng.rag.model.ModelFactory;
import com.feng.rag.model.rerank.RerankResponse;
import com.feng.rag.model.rerank.RerankResult;
import com.feng.rag.model.siliconflow.SiliconflowModel;
import com.feng.rag.retrieval.input.UserInputProcessor;
import com.feng.rag.retrieval.obj.DialogueTurn;
import com.feng.rag.retrieval.obj.ProcessedQuery;
import com.feng.rag.vector.service.VectorService;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 检索服务 - 在线RAG流程的核心服务
 * 职责：协调用户输入处理、检索、重排等流程
 *
 * @author txf
 * @since 2026/3/28
 */
@Service()
@Slf4j
public class RetrievalService {

    private final UserInputProcessor userInputProcessor;
    private final VectorService milvusService;
    private final ModelFactory modelFactory;

    /**
     * 简单的对话历史存储（内存中）
     * 实际生产环境应该使用 Redis 或数据库存储
     * Key: sessionId, Value: 对话历史列表
     */
    private final Map<String, List<DialogueTurn>> dialogueHistoryStore = new ConcurrentHashMap<>();

    /**
     * RAG系统提示词模板
     */
    private static final String RAG_SYSTEM_PROMPT = """
            你是一个智能助手，基于提供的参考资料回答用户问题。

            请遵循以下原则：
            1. 基于参考资料回答，如果资料不足以回答问题，请明确说明
            2. 不要编造信息，如果不确定请如实告知
            3. 回答简洁清晰，突出重点
            4. 可以适当引用参考资料中的内容

            参考资料：
            %s
            """;

    public RetrievalService(UserInputProcessor userInputProcessor,
                            VectorService milvusService,
                            ModelFactory modelFactory) {
        this.userInputProcessor = userInputProcessor;
        this.milvusService = milvusService;
        this.modelFactory = modelFactory;
    }

    //==============================================单纯的向量检索
    /**
     * 单纯的向量检索
     *
     * @param userQuery 用户查询
     * @param topK      返回结果数量
     * @param orgId     组织ID
     * @return 搜索结果列表
     */
    public SearchResp vectorRetrieve(String userQuery, Integer topK, String orgId) {
        log.info("[RetrievalService] 开始向量检索: query={}, topK={}, orgId={}",
                userQuery.substring(0, Math.min(userQuery.length(), 50)), topK, orgId);
        // 1. 用户输入的意图识别与重写
        ProcessedQuery processed = userInputProcessor.process(userQuery);
        // 如果不需要检索（闲聊或敏感词），直接返回空列表
        if (!processed.needsRetrieval()) {
            log.info("[RetrievalService] 无需检索，直接返回: intent={}", processed.getIntent());
            return null;
        }
        // 使用重写后的查询进行检索
        String queryToSearch = processed.getRewrittenQuery() != null
                ? processed.getRewrittenQuery()
                : userQuery;
        // 2. 执行向量检索
        // 使用默认集合
        // 3. 打印输出显示
        // log.info("[RetrievalService] 向量检索完成，响应结果：{}", searchResp.getSearchResults());
        // 4. 返回结果
        return milvusService.vectorSearch(
                queryToSearch,
                topK != null ? topK : 10,
                orgId != null ? orgId : "default",
                null  // 使用默认集合
        );
    }
    /**
     * 单纯的向量检索（使用默认参数）
     */
    public SearchResp vectorRetrieve(String userQuery) {
        return vectorRetrieve(userQuery, 10, "org_id123456"); // TODO 这里先写死
    }


    //==============================================单纯的稀疏检索
    /**
     * 单纯的稀疏检索
     *
     * @param userQuery 用户查询
     * @param topK      返回结果数量
     * @param orgId     组织ID
     * @return 搜索结果列表
     */
    public SearchResp sparseRetrieve(String userQuery, Integer topK, String orgId) {
        log.info("[RetrievalService] 开始稀疏检索: query={}, topK={}, orgId={}",
                userQuery.substring(0, Math.min(userQuery.length(), 50)), topK, orgId);
        // 1. 用户输入的意图识别与重写
        ProcessedQuery processed = userInputProcessor.process(userQuery);
        // 如果不需要检索（闲聊或敏感词），直接返回空
        if (!processed.needsRetrieval()) {
            log.info("[RetrievalService] 无需检索，直接返回: intent={}", processed.getIntent());
            return null;
        }
        // 使用重写后的查询进行检索
        String queryToSearch = processed.getRewrittenQuery() != null
                ? processed.getRewrittenQuery()
                : userQuery;
        // 2. 执行稀疏检索
        SearchResp searchResp = milvusService.sparseSearch(
                queryToSearch,
                topK != null ? topK : 10,
                orgId != null ? orgId : "default",
                null  // 使用默认集合
        );
        // 3. 打印输出显示
        log.info("[RetrievalService] 稀疏检索完成");
        // 4. 返回结果
        return searchResp;
    }
    /**
     * 单纯的稀疏检索（使用默认参数）
     */
    public SearchResp sparseRetrieve(String userQuery) {
        return sparseRetrieve(userQuery, 10, "org_id123456"); // TODO 这里先写死
    }


    //============================================== 混合检索
    public SearchResp hybridRetrieve(String userQuery, Integer topK, String orgId) {
        log.info("[RetrievalService] 开始混合检索: query={}, topK={}, orgId={}",
                userQuery.substring(0, Math.min(userQuery.length(), 50)), topK, orgId);
        // 1. 用户输入的意图识别与重写
        ProcessedQuery processed = userInputProcessor.process(userQuery);
        // 如果不需要检索（闲聊或敏感词），直接返回空
        if (!processed.needsRetrieval()) {
            log.info("[RetrievalService] 无需检索，直接返回: intent={}", processed.getIntent());
            return null;
        }
        // 使用重写后的查询进行检索
        String queryToSearch = processed.getRewrittenQuery() != null
                ? processed.getRewrittenQuery()
                : userQuery;
        // 2. 执行混合检索
        SearchResp searchResp = milvusService.hybridSearch(
                queryToSearch,
                topK != null ? topK : 10,
                orgId != null ? orgId : "default",
                null  // 使用默认集合
        );
        // 3. 打印输出显示
        log.info("[RetrievalService] 混合检索完成");
        // 4. 返回结果
        return searchResp;
    }
    public SearchResp hybridRetrieve(String userQuery) {
        return hybridRetrieve(userQuery, 10, "org_id123456"); // TODO 这里先写死
    }


    //============================================== RAG完整流程：检索 + 重排 + 生成回答 ================================

    /**
     * 完整的RAG流程：检索 → 重排 → 生成回答（同步）
     *
     * @param userQuery 用户查询
     * @param topK      检索结果数量
     * @param topN      重排后取前N个
     * @param orgId     组织ID
     * @param sessionId 会话ID
     * @return 模型生成的回答
     */
    public R ragAnswer(String userQuery, Integer topK, Integer topN, String orgId, String sessionId) {
        log.info("[RetrievalService] 开始完整RAG流程: query={}, topK={}, topN={}",
                userQuery.substring(0, Math.min(userQuery.length(), 50)), topK, topN);
        // 1. 用户输入处理（意图识别 + 查询重写）
        ProcessedQuery processed = userInputProcessor.process(userQuery);
        if (!processed.needsRetrieval()) {
            log.info("[RetrievalService] 无需检索，直接返回: intent={}", processed.getIntent());
            return R.ok().add("answer", "我是AI助手，请问有什么可以帮助您的？");
        }
        String queryToSearch = processed.getRewrittenQuery() != null
                ? processed.getRewrittenQuery()
                : userQuery;
        // 2. 执行混合检索
        SearchResp searchResp = milvusService.hybridSearch(
                queryToSearch,
                topK != null ? topK : 10,
                orgId != null ? orgId : "default",
                null
        );
        if (searchResp == null || searchResp.getSearchResults() == null || searchResp.getSearchResults().isEmpty()) {
            log.warn("[RetrievalService] 未检索到相关文档");
            return R.ok().add("answer", "抱歉，未找到与您问题相关的资料，请尝试其他问题。");
        }
        // 3. 提取文档内容
        List<String> documents = extractDocumentsFromSearchResp(searchResp);
        AbstractModel model = modelFactory.getModel(SiliconflowModel.SILICONFLOW);
        log.info("[RetrievalService] 检索到 {} 个文档，开始重排...", documents.size());
        // 4. 执行重排
        RerankResponse rerankResp = model.rerank(queryToSearch, documents, topN);
        List<String> rankedDocuments;
        if (rerankResp == null || !rerankResp.isSuccess() || rerankResp.getResults() == null) {
            log.warn("[RetrievalService] 重排失败或未实现，使用原始检索结果");
            rankedDocuments = documents.stream().limit(topN != null ? topN : 5).collect(Collectors.toList());
        } else {
            rankedDocuments = rerankResp.getResults().stream()
                    .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                    .map(RerankResult::getDocument)
                    .filter(doc -> doc != null && !doc.isEmpty())
                    .collect(Collectors.toList());
            log.info("[RetrievalService] 重排完成，精选 {} 个文档", rankedDocuments.size());
        }
        // 5. 构建RAG提示词
        String context = String.join("\n\n---\n\n", rankedDocuments);
        String systemPrompt = String.format(RAG_SYSTEM_PROMPT, context);
        // 6. 调用模型生成回答
        List<AbstractModel.Message> messages = List.of(
                new AbstractModel.Message("system", systemPrompt),
                new AbstractModel.Message("user", userQuery)
        );

        log.info("[RetrievalService] 调用模型生成回答...");
        R response = model.chatSync(messages);
        // 7. 保存对话历史（可选）
        saveDialogueHistory(sessionId, userQuery, response);
        log.info("[RetrievalService] RAG流程完成");
        return response;
    }

    /**
     * 完整的RAG流程（流式输出）
     *
     * @param userQuery 用户查询
     * @param topK      检索结果数量
     * @param topN      重排后取前N个
     * @param orgId     组织ID
     * @param sessionId 会话ID
     * @return SSE流
     */
    public SseEmitter ragAnswerStream(String userQuery, Integer topK, Integer topN, String orgId, String sessionId) {
        log.info("[RetrievalService] 开始流式RAG流程: query={}",
                userQuery.substring(0, Math.min(userQuery.length(), 50)));

        SseEmitter emitter = new SseEmitter(0L);
        // 在后台线程执行检索和生成
        new Thread(() -> {
            try {
                // 1. 用户输入处理
                ProcessedQuery processed = userInputProcessor.process(userQuery);
                if (!processed.needsRetrieval()) {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("我是AI助手，请问有什么可以帮助您的？"));
                    emitter.complete();
                    return;
                }
                String queryToSearch = processed.getRewrittenQuery() != null
                        ? processed.getRewrittenQuery()
                        : userQuery;

                // 2. 执行混合检索
                SearchResp searchResp = milvusService.hybridSearch(
                        queryToSearch,
                        topK != null ? topK : 10,
                        orgId != null ? orgId : "default",
                        null
                );

                if (searchResp == null || searchResp.getSearchResults() == null || searchResp.getSearchResults().isEmpty()) {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("抱歉，未找到与您问题相关的资料，请尝试其他问题。"));
                    emitter.complete();
                    return;
                }
                // 3. 提取并重排文档
                List<String> documents = extractDocumentsFromSearchResp(searchResp);
                AbstractModel model = modelFactory.getModel("siliconflow");
                RerankResponse rerankResp = model.rerank(queryToSearch, documents, topN);

                List<String> rankedDocuments;
                if (rerankResp == null || !rerankResp.isSuccess() || rerankResp.getResults() == null) {
                    rankedDocuments = documents.stream().limit(topN != null ? topN : 5).collect(Collectors.toList());
                } else {
                    rankedDocuments = rerankResp.getResults().stream()
                            .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                            .map(RerankResult::getDocument)
                            .filter(doc -> doc != null && !doc.isEmpty())
                            .collect(Collectors.toList());
                }

                // 4. 构建提示词
                String context = String.join("\n\n---\n\n", rankedDocuments);
                String systemPrompt = String.format(RAG_SYSTEM_PROMPT, context);

                List<AbstractModel.Message> messages = List.of(
                        new AbstractModel.Message("system", systemPrompt),
                        new AbstractModel.Message("user", userQuery)
                );
                // 5. 流式生成回答
                SseEmitter modelEmitter = model.chatStream(messages);
                // 转发模型流式输出
                // 注意：这里简化处理，实际应该转发modelEmitter的事件
                emitter.send(SseEmitter.event().name("message").data("正在生成回答..."));
                emitter.complete();
            } catch (Exception e) {
                log.error("[RetrievalService] 流式RAG流程出错", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (Exception ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    /**
     * 简化的RAG回答接口（使用默认参数）
     */
    public R ragAnswer(String userQuery) {
        return ragAnswer(userQuery, 10, 5, "default", null);
    }

    //============================================== 工具方法 =========================================

    /**
     * 从SearchResp中提取文档内容
     */
    private List<String> extractDocumentsFromSearchResp(SearchResp searchResp) {
        List<String> documents = new ArrayList<>();
        if (searchResp == null || searchResp.getSearchResults() == null) {
            return documents;
        }
        for (List<SearchResp.SearchResult> results : searchResp.getSearchResults()) {
            for (SearchResp.SearchResult result : results) {
                // 从entity中提取content字段
                if (result.getEntity() != null && result.getEntity().get("content") != null) {
                    documents.add(result.getEntity().get("content").toString());
                }
            }
        }
        return documents;
    }

    /**
     * 保存对话历史
     */
    private void saveDialogueHistory(String sessionId, String userQuery, R response) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }
        // 实际实现应该保存到Redis或数据库
        // 这里简化处理
        log.debug("[RetrievalService] 保存对话历史: sessionId={}", sessionId);
    }
}
