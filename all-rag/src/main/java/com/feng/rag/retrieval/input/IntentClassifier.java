package com.feng.rag.retrieval.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.rag.controller.R;
import com.feng.rag.model.AbstractModel;
import com.feng.rag.model.ModelFactory;
import com.feng.rag.retrieval.obj.UserIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 意图识别器 - 判断用户输入属于哪种意图
 *
 * @author txf
 * @since 2026/3/29
 */
@Slf4j
@Component
public class IntentClassifier {
    /**
     * 默认使用的模型提供商
     */
    private static final String DEFAULT_PROVIDER = "siliconflow";
    /**
     * 简单的敏感词列表（实际生产环境应该使用更完善的敏感词库）
     */
    private static final List<String> SENSITIVE_KEYWORDS = List.of(
            "暴力", "色情", "赌博", "毒品", "违法"
    );
    /**
     * 闲聊关键词模式
     */
    private static final List<Pattern> CHITCHAT_PATTERNS = List.of(
            Pattern.compile("^(你好|您好|嗨|哈喽|在吗|在么).*"),
            Pattern.compile(".*(谢谢|感谢).*"),
            Pattern.compile(".*(再见|拜拜|bye).*"),
            Pattern.compile("^(你是谁|你叫什么|介绍一下自己).*"),
            Pattern.compile("^(今天天气|现在几点).*")
    );

    /**
     * 意图识别提示词模板
     */
    private static final String SystemPrompt = """
            你是意图识别专家。请分析用户输入，判断属于以下哪种意图：
                1. knowledge_query - 知识问答：用户询问某个知识、概念、方法、产品使用等需要检索才能回答的问题
                2. chitchat - 闲聊：打招呼、感谢、告别、日常寒暄等不需要检索的闲聊
                3. sensitive - 敏感词：涉及暴力、色情、赌博、毒品、违法等内容
            
            请只输出如下JSON格式，不要有任何其他解释：
            {
                "intent": "knowledge_query|chitchat|sensitive",
                "reason": "简要说明判断理由"
            }
            """;

    private final ModelFactory modelFactory;
    public IntentClassifier(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    /**
     * 获取模型实例
     */
    private AbstractModel getModel() {
        return modelFactory.getModel(DEFAULT_PROVIDER);
    }

    /**
     * 识别用户意图
     *
     * @param query 用户输入
     * @return 意图识别结果，包含理由
     */
    public IntentResult classify(String query) {
        log.info("[IntentClassifier] 开始识别意图: {}", query);
        // 1. 先进行规则匹配（快速路径）
        UserIntent ruleBasedIntent = ruleBasedClassify(query);
        if (ruleBasedIntent != UserIntent.UNKNOWN) {
            log.info("[IntentClassifier] 规则匹配识别为: {}", ruleBasedIntent);
            return new IntentResult(ruleBasedIntent, "规则匹配识别");
        }
        // 2. 使用LLM进行意图识别
        return llmClassify(query);
    }

    /**
     * 基于规则的快速分类
     */
    private UserIntent ruleBasedClassify(String query) {
        String lowerQuery = query.toLowerCase();
        // 检查敏感词
        for (String sensitive : SENSITIVE_KEYWORDS) {
            if (lowerQuery.contains(sensitive)) {
                return UserIntent.SENSITIVE;
            }
        }
        // 检查闲聊模式
        for (Pattern pattern : CHITCHAT_PATTERNS) {
            if (pattern.matcher(lowerQuery).matches()) {
                return UserIntent.CHITCHAT;
            }
        }
        return UserIntent.UNKNOWN;
    }

    /**
     * 使用LLM进行意图识别
     */
    private IntentResult llmClassify(String query) {
        String userPrompt = "用户输入：" + query;
        try {
            List<AbstractModel.Message> messages = List.of(
                    new AbstractModel.Message("system", SystemPrompt),
                    new AbstractModel.Message("user", userPrompt)
            );
            R response = getModel().chatSync(messages);
            if (response.getCode() == 200) {
                String aiResponse = (String) response.getData().get("aiRes");
                return parseIntentFromResponse(aiResponse);
            } else {
                log.warn("[IntentClassifier] LLM调用失败，默认返回知识问答");
                return new IntentResult(UserIntent.KNOWLEDGE_QUERY, "LLM调用失败，默认处理");
            }
        } catch (Exception e) {
            log.error("[IntentClassifier] 意图识别异常", e);
            return new IntentResult(UserIntent.KNOWLEDGE_QUERY, "异常，默认处理");
        }
    }

    /**
     * 从AI响应中解析意图
     * 硅基流动响应格式：{"choices":[{"message":{"content":"{\"intent\":\"...\",\"reason\":\"...\"}"}}]}
     */
    private IntentResult parseIntentFromResponse(String aiResponse) {
        // 1. 创建Jackson核心对象
        ObjectMapper objectMapper = new ObjectMapper();
        // 2. 第一步：解析外层JSON，快速提取 content 字段
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(aiResponse);
            String contentJson = rootNode.path("choices")  // 取choices数组
                    .get(0)                               // 取第一个元素
                    .path("message")                      // 取message对象
                    .path("content")                      // 取content字符串
                    .asText();
            // 3. 第二步：将content的JSON转为Record对象
            IntentInfo intentInfo = objectMapper.readValue(contentJson, IntentInfo.class);
            UserIntent userIntent = UserIntent.fromCode(intentInfo.intent);
            return new IntentResult(userIntent, intentInfo.reason); // 返回意图识别结果
        } catch (JsonProcessingException e) {
            log.error("[IntentClassifier] 响应解析异常", e);
            return new IntentResult(UserIntent.KNOWLEDGE_QUERY, "LLM调用失败，默认处理");
        }
    }
    // aiRes对应content里的JSON结构
    public record IntentInfo(
            String intent,    // 知识查询类型
            String reason     // 原因描述
    ) {}
    /**
     * 意图识别结果
     */
    public record IntentResult(UserIntent intent, String reason) {
    }
}
