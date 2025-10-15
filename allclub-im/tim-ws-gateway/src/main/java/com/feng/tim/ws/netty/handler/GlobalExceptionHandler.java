package com.feng.tim.ws.netty.handler;

import com.feng.tim.ws.connection.ConnectionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: 全局异常处理器
 * @Author: txf
 * @Date: 2025/10/12
 */
@Component
@ChannelHandler.Sharable
@Slf4j
public class GlobalExceptionHandler extends ChannelInboundHandlerAdapter {
    @Resource
    private ConnectionManager connectionManager;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 记录日志并关闭连接
        log.error("全局异常: {}", cause.getMessage());
        log.error("exceptionCaught: {}", cause.getCause().toString());

        connectionManager.remove(connectionManager.getByChannel(ctx.channel()));

        ctx.close();
    }
}
