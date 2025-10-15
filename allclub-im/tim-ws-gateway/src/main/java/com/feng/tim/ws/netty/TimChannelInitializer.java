package com.feng.tim.ws.netty;

import com.feng.tim.ws.config.WsGatewayConfig;
import com.feng.tim.ws.netty.handler.GlobalExceptionHandler;
import com.feng.tim.ws.netty.handler.MessageRouteHandler;
import com.feng.tim.ws.netty.handler.WsConnectionPerception;
import com.feng.tim.ws.netty.handler.WsShakeInterceptorHandler;
import com.feng.tim.ws.protocol.code.TMessageDecoder;
import com.feng.tim.ws.protocol.code.TMessageEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Description: Channel初始化器
 * @Author: txf
 * @Date: 2025/10/11
 */
@Component
public class TimChannelInitializer extends ChannelInitializer<NioSocketChannel> {
    private static final String defaultContext = "/tim";

    @Resource(name = "wsGatewayConfig")
    private WsGatewayConfig conf;
    @Resource
    private TMessageEncoder tMessageEncoder;
    @Resource
    private TMessageDecoder tMessageDecoder;
    @Resource
    private WsShakeInterceptorHandler wsShakeInterceptorHandler; // 握手拦截器
    @Resource
    private WsConnectionPerception wsConnectionPerception; // 链接感知器
    @Resource
    private GlobalExceptionHandler globalExceptionHandler; // 全局异常处理器
    @Resource
    private MessageRouteHandler messageRouteHandler; // 消息路由处理器

    // 业务线程池
    @Resource(name = "businessEventExecutor1")
    private DefaultEventExecutorGroup businessEventExecutor;
    // 路由业务线程池
    @Resource(name = "routeEventExecutor1")
    private DefaultEventExecutorGroup routeEventExecutor;

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 1.HTTP 服务基础：解码/聚合
        pipeline.addLast(new HttpServerCodec())// 添加HTTP编解码器
                .addLast(new HttpObjectAggregator(65536)); // 聚合HTTP消息，最大64KB
        // 2.【ws握手拦截器】--(权限验证)
        pipeline.addLast(businessEventExecutor, "shake-interceptor-handler" ,wsShakeInterceptorHandler);
        // 3.WebSocket协议：握手 + 压缩（先压缩，再握手）
        pipeline.addLast(new WebSocketServerCompressionHandler())  // 添加WebSocket消息压缩处理器 【入站出站】
                .addLast(new WebSocketServerProtocolHandler(context())); // 添加WebSocket协议处理器 【入站出站】
        // 3.1 连接感知器
        pipeline.addLast(businessEventExecutor, "connection-perception", wsConnectionPerception);
        // 4.消息编解码
        pipeline.addLast(tMessageDecoder).addLast(tMessageEncoder);
        // 5.业务处理
        pipeline.addLast(routeEventExecutor, "message-route-handler", messageRouteHandler);
        // 6.异常处理
        pipeline.addLast(globalExceptionHandler);
    }

    private String context() {
        if ( conf.getContext() == null || conf.getContext().isEmpty() ||
                !conf.getContext().startsWith("/") || conf.getContext().endsWith("/") ||
                conf.getContext().contains(" ") || conf.getContext().length() == 1) {
            return defaultContext;
        }
        return conf.getContext();
    }
}
