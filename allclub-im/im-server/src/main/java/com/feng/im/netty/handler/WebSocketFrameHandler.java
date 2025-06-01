package com.feng.im.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @Description: 处理websocket帧
 * @Author: txf
 * @Date: 2025/5/5
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        ByteBuf content = msg.content().retain(); // 保留引用计数
        System.out.println("WebSocketFrameHandler " + content.refCnt());
        ctx.fireChannelRead(content);
        // 将WebSocket文本帧转换为TMessage对象
        // TMessage tMsg = JSON.parseObject(msg.text(), TMessage.class);
        // // 继续后续业务处理
        // ctx.pipeline().fireChannelRead(tMsg);
    }
}
