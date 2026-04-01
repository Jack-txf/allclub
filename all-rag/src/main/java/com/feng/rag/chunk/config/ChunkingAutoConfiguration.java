package com.feng.rag.chunk.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 分块模块自动配置类
 *
 * <p>启用分块模块的所有组件：</p>
 * <ul>
 *   <li>ChunkingProperties - 配置属性</li>
 *   <li>各种分块策略</li>
 *   <li>ChunkingService - 分块服务</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties(ChunkingProperties.class)
@ComponentScan(basePackages = "com.feng.rag.chunk")
public class ChunkingAutoConfiguration {
    // 自动配置通过 @ComponentScan 完成
}
