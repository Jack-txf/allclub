package com.feng.im.netty.handler;


import com.feng.im.api.model.User;
import com.feng.im.init.UserDatas;
import com.feng.im.netty.ChannelAttributeKeys;
import com.feng.im.netty.ChannelManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: websocket拦截
 * @Author: txf
 * @Date: 2025/5/5
 */
@Slf4j
public class WebSocketInterceptor extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final UserDatas userDatas;
    private final ChannelManager channelManager;

    public WebSocketInterceptor(UserDatas userDatas, ChannelManager channelManager) {
        this.userDatas = userDatas;
        this.channelManager = channelManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 1. 检查是否为 WebSocket 握手请求
        if (isWebSocketUpgrade(request)) {
            // 2. 验证权限
            if (authorize(ctx, request)) {
                // 3. 验证通过，将请求传递给下一个处理器
                request.retain();
                System.out.println("WebSocketInterceptor " + request.content().refCnt());
                ctx.fireChannelRead(request);
            } else {
                // 4. 验证失败，返回 401 并关闭连接
                sendUnauthorizedResponse(ctx, request);
                ctx.close();
            }
        }
        else {
            // 不是握手请求
            handleHttpRequest(ctx, request);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 处理普通 HTTP 请求（可选）
        log.info("收到普通HTTP请求：{}", request.uri());
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer("该服务端口不支持WebSocket之外的请求!".getBytes())
        );
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    // 响应未认证成功
    private void sendUnauthorizedResponse(ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.UNAUTHORIZED,
                Unpooled.wrappedBuffer("Unauthorized".getBytes())
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

    // 检查是否为 WebSocket 握手请求
    private boolean isWebSocketUpgrade(FullHttpRequest request) {
        return request.headers().contains(HttpHeaderNames.UPGRADE)
                && request.headers().get(HttpHeaderNames.UPGRADE).equalsIgnoreCase("websocket");
    }

    // 鉴权
    private boolean authorize(ChannelHandlerContext ctx, FullHttpRequest req) {
        String token = req.headers().get("tim-token");
        if (token != null ) {
            User user = userDatas.parseUserByToken(token);
            if (user != null) { // 说明登录了
                // 方式二: 给下一个处理器来处理--这里通过给channel添加一个附件，附件内容是userId
                ctx.channel().attr(ChannelAttributeKeys.USER_ID).set(user.getUid()); // 给这个channel绑定一个附件

                // 方式一：认证通过后就放入channel
                //log.info("用户 {} 连接TIM成功", user.getUsername());
                //channelManager.addUserChannel(user.getUid(), ctx.channel());
                return true;
            }
            return false;
        }
        // 从URI中提取：?token=xxx
        // QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        // if (decoder.parameters().containsKey("token")) {
        //     String token = decoder.parameters().get("token").get(0);
        //     return TokenService.verify(token);
        // }
        return false;
    }
}
