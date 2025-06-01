package com.feng.im.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.Getter;

/**
 * @version 1.0
 * @Author
 * @Date 2025/4/2 16:38
 * @注释
 */
@Data
public class NettyWebsocketServer {
    private int port = 19999;
    private NettyInitializer nettyInitializer;
    @Getter
    private static final NettyWebsocketServer instance = new NettyWebsocketServer();

    private NettyWebsocketServer() {
    }

    // 启动服务
    public void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(16);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(nettyInitializer);
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            System.out.println("netty-websocket服务启动成功！ port: " + port);

            channelFuture.channel().closeFuture().sync();

        } catch ( Exception e ) {
            System.out.println("netty服务启动失败！");

        } finally {
            // 优雅地关闭EventLoopGroup
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            System.out.println("Netty服务器已关闭");
        }
    }
}
