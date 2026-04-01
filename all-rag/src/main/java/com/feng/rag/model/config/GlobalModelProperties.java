package com.feng.rag.model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 全局模型配置类
 * @Author: txf
 * @Date: 2026/3/24
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai-model")
public class GlobalModelProperties {
    // 模型提供商
    private Map<String, ProviderConfig> providers = new HashMap<>();

    @Data
    public static class ProviderConfig {
        /**
         * 提供商基础 URL
         */
        private String baseUrl;

        /**
         * API 密钥
         */
        private String apiKey;

        // 【这个是chatmodel的流式配置】streaming，默认是
        private String chatStreaming;

        // 三种模型名字
        private List<String> chatModel;
        private List<String> embedModel;
        private List<String> rerankModel;
    }
}
