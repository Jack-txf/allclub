package com.feng.im.config;


import com.feng.im.netty.NettyInitializer;
import com.feng.im.netty.NettyWebsocketServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/5/5
 */
@Configuration
public class NettyInstanceConfig {

    // 创建一个NettyWebsocketServer的实例
    @Bean
    public NettyWebsocketServer nettyWebsocketServer(NettyInitializer nettyInitializer) {
        NettyWebsocketServer instance = NettyWebsocketServer.getInstance();
        instance.setNettyInitializer(nettyInitializer);
        return instance;
    }
}
