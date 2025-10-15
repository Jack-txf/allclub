package com.feng.tim.ws.netty.handler;

import com.feng.tim.ws.connection.ConnectionManager;
import com.feng.tim.ws.netty.msgstrategy.MessageStrategy;
import com.feng.tim.ws.netty.msgstrategy.StrategyFactory;
import com.feng.tim.ws.protocol.TMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: 消息路由处理，调用业务逻辑处理
 * @Author: txf
 * @Date: 2025/10/12
 */
@Component
@ChannelHandler.Sharable
@Slf4j
public class MessageRouteHandler extends SimpleChannelInboundHandler<TMessage> {

    @Resource
    private StrategyFactory strategyFactory;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TMessage tMessage) throws Exception {
        String msgType = tMessage.getType(); // 获取消息type
        MessageStrategy strategy = strategyFactory.getStrategy(msgType);
        strategy.tackle(tMessage);
    }
}
