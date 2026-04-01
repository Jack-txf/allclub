package com.feng.rag.retrieval.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.rag.controller.R;
import com.feng.rag.model.AbstractModel;
import com.feng.rag.model.ModelFactory;
import com.feng.rag.retrieval.obj.DialogueTurn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 查询重写器 - 将用户当前问题结合对话历史，重写成独立完整的查询
 * @author txf
 * @since 2026/3/29
 */
@Slf4j
@Component
public class QueryRewriter {

    private static final String DEFAULT_PROVIDER = "siliconflow";

    private final ModelFactory modelFactory;
    public QueryRewriter(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    private AbstractModel getModel() {
        return modelFactory.getModel(DEFAULT_PROVIDER);
    }

    //
    private static final String SystemPrompt = """
            你是查询重写专家。请将用户的当前问题结合历史对话，重写成一个独立、完整、适合检索的查询。

            要求：
            1. 解决指代问题：将"它"、"这个"、"那个"等代词替换为具体名词
            2. 补充省略信息：根据历史对话补全省略的主语、宾语
            3. 去除口语化：将口语化表达转为正式、清晰的检索语句
            4. 保持原意：不改变用户问题的核心意图
            5. 只输出重写后的查询，不要有任何解释

            示例如下：
            历史：用户问"Java怎么读取文件？" AI回答"使用FileReader..."
            当前："那写入呢？"
            重写："Java怎么写入文件？"
            """;

    /**
     * 重写查询
     *
     * @param currentQuery    用户当前问题
     * @param dialogueHistory 对话历史（最近N轮）
     * @return 重写后的独立完整查询
     */
    public String rewrite(String currentQuery, List<DialogueTurn> dialogueHistory) {
        log.info("[QueryRewriter] 开始重写查询: {}", currentQuery);
        // 如果没有历史，直接返回当前查询
        // if (dialogueHistory == null || dialogueHistory.isEmpty()) {
        //     log.info("[QueryRewriter] 无对话历史，直接返回原查询");
        //     return currentQuery;
        // }
        // 构建历史对话文本 TODO 后续从数据库读取
        // String historyText = buildHistoryText(dialogueHistory);
        String historyText = generateHistory();
        // String historyText = "";

        String userPrompt = String.format("""
                历史对话如下：
                %s

                当前问题是这个：%s

                请重写：
                """, historyText, currentQuery);
        try {
            List<AbstractModel.Message> messages = List.of(
                    new AbstractModel.Message("system", SystemPrompt),
                    new AbstractModel.Message("user", userPrompt)
            );
            R response = getModel().chatSync(messages);
            if (response.getCode() == 200) {
                String aiResponse = (String) response.getData().get("aiRes");
                String rewritten = extractContentFromResponse(currentQuery, aiResponse);
                log.info("[QueryRewriter] 重写结果: {} -> {}", currentQuery, rewritten);
                return rewritten;
            } else {
                log.warn("[QueryRewriter] LLM调用失败，返回原查询");
                return currentQuery;
            }
        } catch (Exception e) {
            log.error("[QueryRewriter] 查询重写异常", e);
            return currentQuery;
        }
    }

    /**
     * 模拟生成一个历史记录
     */
    private String generateHistory() {
        return """
                用户：介绍一下Kafka呗！
                AI：Kafka 是一个分布式的基于发布 / 订阅模式的消息队列（Message Queue），主要应用于大数据实时处理领域。
                """;
    }

    /**
     * 构建历史对话文本
     */
    private String buildHistoryText(List<DialogueTurn> dialogueHistory) {
        StringBuilder sb = new StringBuilder();

        int start = Math.max(0, dialogueHistory.size() - 3); // 只取最近3轮
        for (int i = start; i < dialogueHistory.size(); i++) {
            DialogueTurn turn = dialogueHistory.get(i);
            sb.append("用户：").append(turn.getUserQuery()).append("\n");
            if (turn.getAiResponse() != null) {
                // AI回答可能很长，只取前200字符
                String aiResp = turn.getAiResponse();
                if (aiResp.length() > 200) {
                    aiResp = aiResp.substring(0, 200) + "...";
                }
                sb.append("AI：").append(aiResp).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 从AI响应中提取内容
     * 简单处理：去除JSON标记，提取content或直接用整个响应
     */
    private String extractContentFromResponse(String currentQuery, String aiResponse) {
        // 尝试提取 content 字段
        // 1. 创建Jackson核心对象
        ObjectMapper objectMapper = new ObjectMapper();
        // 2. 第一步：解析外层JSON，快速提取 content 字段
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(aiResponse);
            return rootNode.path("choices")  // 取choices数组
                    .get(0)                               // 取第一个元素
                    .path("message")                      // 取message对象
                    .path("content")                      // 取content字符串
                    .asText(); // 返回意图识别结果
        } catch (JsonProcessingException e) {
            log.error("[QueryRewriter] 响应解析异常", e);
            return currentQuery;
        }
    }
}
