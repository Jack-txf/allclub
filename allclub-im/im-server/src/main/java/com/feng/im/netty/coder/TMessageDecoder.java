package com.feng.im.netty.coder;

import com.alibaba.fastjson2.JSON;
import com.feng.im.netty.TMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @version 1.0
 * @Author
 * @Date 2025/4/7 11:42
 * @注释 消息解码器
 */
public class TMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1. 判断是否有可读字节
        int readable = in.readableBytes();
        if (readable == 0) {
            return;  // 还没收到数据，等待下次调用
        }
        // 2.读取所有字节到 byte[]
        byte[] bytes = new byte[readable];
        in.readBytes(bytes);
        // 3.将JSON bytes 反序列化为 TMessage
        TMessage msg = JSON.parseObject(bytes, TMessage.class);
        // 4.交给下一个 Handler 处理
        out.add(msg);
    }
}
