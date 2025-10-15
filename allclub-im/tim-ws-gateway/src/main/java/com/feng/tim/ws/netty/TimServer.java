package com.feng.tim.ws.netty;

import com.feng.tim.ws.config.WsGatewayConfig;
import com.feng.tim.ws.dubbo.IMServerTackle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: Tim的服务
 * @Author: txf
 * @Date: 2025/10/11
 */
@Slf4j
@Component
public class TimServer {
    @Resource(name = "wsGatewayConfig")
    private WsGatewayConfig conf;
    @Resource(name = "timChannelInitializer")
    private TimChannelInitializer initializer;

    // 测试dubbo调用
    @Resource
    private IMServerTackle imServerTackle;

    private ServerBootstrap server;
    private NioEventLoopGroup boss = null;
    private NioEventLoopGroup worker = null;

    private void init() {
        boss = new NioEventLoopGroup(2);
        worker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        server = new ServerBootstrap(); // 创建服务端启动类

        String res = imServerTackle.imServer("ggg");
        log.info("dubbo调用结果: {}", res);
    }
    public void start() {
        init();
        try {
            // 绑定线程组，进行一些配置
            server.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024) // 服务端可连接队列
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 保持长连接
                    .childHandler(initializer);
            ChannelFuture bindFuture = server.bind(conf.getPort()).sync();
            log.info("ws启动服务成功, 地址: ws://127.0.0.1:{}{}" , conf.getPort(), conf.getContext());
            log.info("应用名称:{}", conf.getAppName());
            bindFuture.channel().closeFuture().sync();
            log.info("ws服务已关闭");
        } catch (InterruptedException e) {
            log.warn("启动服务失败, {}" , e.getMessage());
        } finally {
            if ( boss != null) boss.shutdownGracefully();
            if ( worker != null) worker.shutdownGracefully();
        }
        log.info("ws网关启动成功");
    }
}
