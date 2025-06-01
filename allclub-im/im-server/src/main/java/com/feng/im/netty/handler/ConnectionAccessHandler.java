package com.feng.im.netty.handler;


import com.feng.im.netty.ChannelAttributeKeys;
import com.feng.im.netty.ChannelManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/5/5
 */
@Slf4j
public class ConnectionAccessHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final ChannelManager channelManager;
    public ConnectionAccessHandler(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        Attribute<String> attr = ctx.channel().attr(ChannelAttributeKeys.USER_ID);
        String uid = attr.get();
        if (uid != null && isWebSocketUpgrade(msg)) {
            log.info("用户 {} 连接上了TIM！", uid);
            channelManager.addUserChannel(uid, ctx.channel());
        }
        int i = msg.content().refCnt();
        msg.retain();
        System.out.println(" ConnectionAccessHandler " + i);
        ctx.fireChannelRead(msg); // 传递给下一个处理器
    }

    // 检查是否为 WebSocket 握手请求
    private boolean isWebSocketUpgrade(FullHttpRequest request) {
        return request.headers().contains(HttpHeaderNames.UPGRADE)
                && request.headers().get(HttpHeaderNames.UPGRADE).equalsIgnoreCase("websocket");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接断开了： {}", ctx.channel().remoteAddress());
        channelManager.removeUserChannel(ctx.channel());
        super.channelInactive(ctx);
    }
}
