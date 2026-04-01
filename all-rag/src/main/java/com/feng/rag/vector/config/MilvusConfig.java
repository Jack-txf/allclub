package com.feng.rag.vector.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Milvus 配置类
 * @author txf
 * @since 2026/3/26
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MilvusProperties.class)
@ConditionalOnProperty(name = "rag.vector.type", havingValue = "milvus", matchIfMissing = true)
public class MilvusConfig {

    private MilvusClientV2 milvusClient;

    /**
     * 构建 Milvus 客户端 Bean
     */
    @Bean(destroyMethod = "close")
    public MilvusClientV2 milvusClient(MilvusProperties properties) {
        log.info("[MilvusConfig] 初始化 Milvus 客户端: uri={}", properties.getUri());
        ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder()
                .uri(properties.getUri())
                .connectTimeoutMs(properties.getConnectTimeoutMs());
                // .requestTimeoutMs(properties.getRequestTimeoutMs());

        // 如果配置了 Token，添加认证
        if (properties.getToken() != null && !properties.getToken().isEmpty()) {
            builder.token(properties.getToken());
        }
        milvusClient = new MilvusClientV2(builder.build());
        log.info("[MilvusConfig] Milvus 客户端初始化成功");
        return milvusClient;
    }

    /**
     * 应用关闭时释放资源
     */
    @PreDestroy
    public void destroy() {
        if (milvusClient != null) {
            log.info("[MilvusConfig] 关闭 Milvus 客户端");
            milvusClient.close();
        }
    }
}
