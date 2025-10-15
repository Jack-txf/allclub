package com.feng.tim.ws.netty.handler;

import ch.qos.logback.classic.Logger;
import com.feng.tim.ws.connection.ConnectionManager;
import com.feng.tim.ws.netty.keys.ChannelAttributeKeys;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: ws连接感知
 * @Author: txf
 * @Date: 2025/7/23
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WsConnectionPerception extends ChannelInboundHandlerAdapter {
    @Resource
    private ConnectionManager connectionManager;

    // 连接断开了
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 连接断开
        Channel channel = ctx.channel();
        log.info("连接断开: {}", channel.remoteAddress());
        // 移除连接
        connectionManager.remove(connectionManager.getByChannel(channel));

        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 处理空闲事件
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                // 读空闲，可能客户端已断开连接
                log.info("连接超时，关闭连接: {}", ctx.channel().remoteAddress());
                Channel channel = ctx.channel();
                connectionManager.remove(connectionManager.getByChannel(channel));

                channel.close();
            }
        }
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            log.info("握手完成, tim-authPass: 新的认证通过了 {}", ctx.channel().remoteAddress());
            // 认证通过了才设置进来的
            String username = ctx.channel().attr(ChannelAttributeKeys.USERNAME).get();
            Long uid = ctx.channel().attr(ChannelAttributeKeys.UID).get();
            // 构建连接信息放进去
            connectionManager.register(uid, ctx.channel());

            log.info("当前会话中心人数：{}", connectionManager.getTotalCount());
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.fireChannelRead(msg);
    }

}
