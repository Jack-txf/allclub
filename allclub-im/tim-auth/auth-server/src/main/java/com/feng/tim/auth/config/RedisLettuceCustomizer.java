package com.feng.tim.auth.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.stereotype.Component;

/*
这样 Lettuce 就会：
启动 TCP 层 keepalive；
在使用连接前主动 PING 检查；
发现连接断开后自动重连；
避免 NAT 导致的假死连接。
 */
@Component
public class RedisLettuceCustomizer implements LettuceClientConfigurationBuilderCustomizer {
    @Override
    public void customize(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        builder.clientOptions(ClientOptions.builder()
                .autoReconnect(true)
                .socketOptions(SocketOptions.builder()
                        .keepAlive(true) // 打开 TCP keepalive
                        .build())
                .pingBeforeActivateConnection(true) // 在激活前发送 PING
                .build());
    }
}
