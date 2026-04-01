package com.feng.rag.retrieval.input;

import com.feng.rag.retrieval.obj.DialogueTurn;
import com.feng.rag.retrieval.obj.ProcessedQuery;
import com.feng.rag.retrieval.obj.UserIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 用户输入处理器 - 在线RAG流程的第一步
 * 职责：意图识别 + 查询重写
 *
 * @author txf
 * @since 2026/3/29
 */
@Slf4j
@Service
public class UserInputProcessor {

    private final IntentClassifier intentClassifier;
    private final QueryRewriter queryRewriter;

    public UserInputProcessor(IntentClassifier intentClassifier,
                              QueryRewriter queryRewriter) {
        this.intentClassifier = intentClassifier;
        this.queryRewriter = queryRewriter;
    }

    /**
     * 处理用户输入（带对话历史）
     * @param userQuery       用户原始输入
     * @param dialogueHistory 对话历史（最近N轮）
     * @return 处理后的查询结果
     */
    public ProcessedQuery process(String userQuery, List<DialogueTurn> dialogueHistory) {
        log.info("[UserInputProcessor] 开始处理用户输入: {}", userQuery);
        // 1. 意图识别
        IntentClassifier.IntentResult intentResult = intentClassifier.classify(userQuery);
        UserIntent intent = intentResult.intent();
        log.info("[UserInputProcessor] 意图识别结果: {} - {}", intent.getDescription(), intentResult.reason());
        // 2. 根据意图决定后续处理
        ProcessedQuery.ProcessedQueryBuilder resultBuilder = ProcessedQuery.builder()
                .originalQuery(userQuery)
                .intent(intent)
                .intentReason(intentResult.reason());
        // 3. 敏感词直接返回，不处理
        if (intent.shouldReject()) {
            log.warn("[UserInputProcessor] 检测到敏感词，直接拒绝");
            resultBuilder.rewrittenQuery(null);
            return resultBuilder.build();
        }
        // 4. 闲聊也直接返回，不重写
        if (intent.equals(UserIntent.CHITCHAT)) {
            log.info("[UserInputProcessor] 闲聊类型，直接返回");
            resultBuilder.rewrittenQuery(userQuery);
            return resultBuilder.build();
        }
        // 5. 知识问答 - 进行查询重写
        String rewrittenQuery = queryRewriter.rewrite(userQuery, dialogueHistory);
        resultBuilder.rewrittenQuery(rewrittenQuery);
        log.info("[UserInputProcessor] 重写处理完成: original='{}', rewritten='{}'",
                userQuery, rewrittenQuery);
        return resultBuilder.build();
    }

    /**
     * 处理用户输入（无对话历史）
     *
     * @param userQuery 用户原始输入
     * @return 处理后的查询结果
     */
    public ProcessedQuery process(String userQuery) {
        return process(userQuery, null);
    }
}
