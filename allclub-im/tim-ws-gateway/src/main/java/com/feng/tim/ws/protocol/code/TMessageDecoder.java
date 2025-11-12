package com.feng.tim.ws.protocol.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.tim.ws.connection.ConnectionManager;
import com.feng.tim.ws.protocol.TMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 消息解码器
 * @Author: txf
 * @Date: 2025/10/11
 */
@Slf4j
@Component
@ChannelHandler.Sharable // 如果你的 Handler 无状态，建议加上
public class TMessageDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {
    @Resource
    private ConnectionManager connectionManager;

    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame frame, List<Object> out) throws Exception {
        TMessage msg;
        try {
            msg = mapper.readValue(frame.text(), TMessage.class);
            // 更新当前channel的活跃时间
            connectionManager.updateActiveTime(ctx.channel());
        } catch (Exception e) {
            log.error("消息解码失败：{}", e.getMessage());
            msg = TMessage.error("NULL", frame.text());
        }
        out.add(msg);
    }
}
