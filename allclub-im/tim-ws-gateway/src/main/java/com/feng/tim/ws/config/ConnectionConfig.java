package com.feng.tim.ws.config;

import com.feng.tim.ws.connection.ConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/10/12
 */
@Configuration
public class ConnectionConfig {
    @Bean("connectionManager")
    public ConnectionManager connectionManager(RedisTemplate<String, Object> redisTemplate,
                                               WsGatewayConfig wsGatewayConfig) {
        return new ConnectionManager(redisTemplate, wsGatewayConfig);
    }
}
