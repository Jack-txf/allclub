package com.feng.im.netty;



import com.feng.im.init.UserDatas;
import com.feng.im.netty.coder.TMessageDecoder;
import com.feng.im.netty.coder.TMessageEncoder;
import com.feng.im.netty.handler.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @version 1.0
 * @Author txf
 * @Date 2025/4/2 17:04
 * @注释 netty初始化
 */
@Component
public class NettyInitializer extends ChannelInitializer<SocketChannel> {
    @Resource
    private UserDatas userDatas;
    @Resource
    private ChannelManager channelManager;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 1.HTTP 服务基础：解码/聚合
        pipeline.addLast(new HttpServerCodec())// 添加HTTP编解码器
                .addLast(new HttpObjectAggregator(1024*64));// 聚合HTTP消息，最大64KB
        // 授权拦截器：在握手前拦截并校验
        pipeline.addLast(new WebSocketInterceptor(userDatas, channelManager)) // 授权拦截器
                .addLast(new ConnectionAccessHandler(channelManager)); // 连接建立处理器
        // 2.WebSocket协议：握手 + 压缩（先压缩，再握手）
        pipeline.addLast(new WebSocketServerCompressionHandler())  // 添加WebSocket消息压缩处理器 【入站出站】
                .addLast(new WebSocketServerProtocolHandler("/tim")) // 添加WebSocket协议处理器 【入站出站】
                .addLast(new WebSocketFrameHandler());  // 处理WebSocket帧 -- text帧转为ByteBuf 【入站】
        // 3.TMessage消息处理
        pipeline.addLast(new TMessageDecoder())// 添加TMessage解码器 ByteBuf -> TMessage
                .addLast(new TMessageEncoder())// 添加TMessage编码器 TMessage -> TextWebsocketFrame
                .addLast(new TMessageHandler(channelManager)) // 添加TMessage业务处理器
                .addLast(new GlobalExceptionHandler(channelManager)); // 添加全局异常处理器
    }
}
