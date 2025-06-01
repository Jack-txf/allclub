package com.feng.im.netty.handler;


import com.feng.im.netty.ChannelManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

// 全局异常处理器
@Slf4j
public class GlobalExceptionHandler extends ChannelInboundHandlerAdapter {

    private final ChannelManager channelManager;

    public GlobalExceptionHandler(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 记录日志并关闭连接
        cause.printStackTrace();
        log.error("全局异常: {}", cause.getMessage());
        channelManager.removeUserChannel(ctx.channel());
        ctx.close();
    }
}