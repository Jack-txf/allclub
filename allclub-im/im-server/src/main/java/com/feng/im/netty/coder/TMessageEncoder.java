package com.feng.im.netty.coder;

import com.alibaba.fastjson2.JSON;
import com.feng.im.netty.TMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

/**
 * @version 1.0
 * @Author txf
 * @Date 2025/4/7 11:42
 * @注释 消息编码器
 */
public class TMessageEncoder extends MessageToMessageEncoder<TMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, TMessage msg, List<Object> out) throws Exception {
        // 1. 序列化为 JSON 字符串
        String json = JSON.toJSONString(msg);
        // 2. 包装到 TextWebSocketFrame
        out.add(new TextWebSocketFrame(json));
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        // 仅对 TMessage 对象生效
        return msg instanceof TMessage;
    }
}
