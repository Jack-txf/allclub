package com.feng.tim.ws.protocol.code;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.tim.ws.protocol.TMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 消息编码器
 * @Author: txf
 * @Date: 2025/10/11
 */
@Slf4j
@Component
@ChannelHandler.Sharable // 如果你的 Handler 无状态，建议加上
public class TMessageEncoder extends MessageToMessageEncoder<TMessage> {
    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    protected void encode(ChannelHandlerContext ctx, TMessage tMessage, List<Object> out) throws Exception {
        String json = null;
        try {
            json = mapper.writeValueAsString(tMessage);
        } catch (Exception e) {
            log.error("消息编码器转换json异常 {}", e.getMessage());
            // TODO 待处理
        }
        out.add(new TextWebSocketFrame(json));
    }
}
