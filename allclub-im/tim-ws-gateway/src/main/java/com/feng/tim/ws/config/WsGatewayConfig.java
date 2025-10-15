package com.feng.tim.ws.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @Description: ws网关配置
 * @Author: txf
 * @Date: 2025/10/11
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "ws.gateway")
public class WsGatewayConfig {
    private Integer port;
    private String appName;
    private String context;
    private List<String> listenTopic;
}
