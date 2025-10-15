package com.feng.tim.ws.netty.handler;

import com.feng.tim.ws.constant.Token;
import com.feng.tim.ws.jwt.JwtUtils;
import com.feng.tim.ws.netty.keys.ChannelAttributeKeys;
import com.feng.tim.ws.obj.TokenUser;
import com.feng.tim.ws.redis.RedisKey;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Description: ws握手拦截器
 * @Author: txf
 * @Date: 2025/10/11
 */
// 只对FullHttpRequest消息感兴趣！！！！！！
@Slf4j
@Component
@ChannelHandler.Sharable // 如果你的 Handler 无状态，建议加上
public class WsShakeInterceptorHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 1.检查是否为握手请求
        if (isWebSocketUpgrade(request)) {
            HttpHeaders headers = request.headers();
            // 2.处理token
            String token = headers.get(Token.TOKEN_PREFIX);
            TokenUser tokenUser;
            // 2.1 校验操作
            if ( (tokenUser = tackleToken(token)) != null
                    && isLoginFromRedis(tokenUser)) {
                // 3.1 校验通过了，附件写入channel
                ctx.channel().attr(ChannelAttributeKeys.USERNAME).set(tokenUser.getUsername());
                ctx.channel().attr(ChannelAttributeKeys.UID).set(tokenUser.getUid());

                request.retain(); // 这里需要引用数加一，因为SimpleChannelInboundHandler会自动释放减一
                ctx.fireChannelRead(request); // 继续处理下一个 ChannelInboundHandler
            } else { // 3.2 校验不通过
                // 响应错误然后关闭连接
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.UNAUTHORIZED,
                        Unpooled.wrappedBuffer("token错误!".getBytes())
                );
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            // 不是握手请求
            handleHttpRequest(ctx, request);
        }
    }
    // 校验redis中是不是登录了
    private boolean isLoginFromRedis(TokenUser tokenUser) {
        boolean b = false;
        try {
            b = redisTemplate.hasKey(RedisKey.userTokenKey(tokenUser.getUid()));
        } catch (Exception e) {
            log.error("redis服务器异常 {}", e.getMessage());
        }
        return b;
    }

    // 处理token
    private TokenUser tackleToken(String token) {
        if ( token == null ) return null;
        if ( jwtUtils.isTokenValid(token)) {
            return jwtUtils.getClaimFromToken(token, (c) ->
                    new TokenUser((String) c.get("uid"), (String) c.get("username")));
        }
        return null;
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

    // 检查是否为 WebSocket 握手请求
    private boolean isWebSocketUpgrade(FullHttpRequest request) {
        return request.headers().contains(HttpHeaderNames.UPGRADE)
                && request.headers().get(HttpHeaderNames.UPGRADE).equalsIgnoreCase("websocket");
    }
}
