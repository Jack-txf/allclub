package com.feng.im.netty.handler;


import com.feng.im.myenum.MsgType;
import com.feng.im.netty.ChannelManager;
import com.feng.im.netty.TMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @version 1.0
 * @Author
 * @Date 2025/4/2 17:09
 * @注释
 */
@Slf4j
public class TMessageHandler extends SimpleChannelInboundHandler<TMessage> {
    private final ChannelManager channelManager;
    public TMessageHandler( ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TMessage tMessage) throws Exception {
        System.out.println("【服务端收到客户端】：" + tMessage);
        if ( tMessage.getType().equals(MsgType.CHAT.getIndex())) { // 聊天消息
            sendMsg(tMessage);
        } else {
            // 写回试一下 ok的
            ctx.writeAndFlush(tMessage);
        }
    }

    private void sendMsg( TMessage tMessage ) {
        List<String> tos = tMessage.getTo();
        if (tos == null || tos.isEmpty()) {
            log.info("消息发送失败，接收者列表为空");
            return;
        }
        if ( tos.size() == 1 ) { // 单聊
            sendSingleton( tMessage);
        } else { // 群聊
            sendGroup(tMessage);
        }
    }
    // 群聊
    private void sendGroup( TMessage tMessage ) {
        List<String> tos = tMessage.getTo();
        for (String uid : tos) {
            Channel channel = channelManager.getUserChannel(uid);
            if (channel == null) {
                log.info("群聊 用户{}目前不在线", uid);
            } else {
                channel.writeAndFlush(tMessage);
            }
        }
    }
    // 单聊
    private void sendSingleton( TMessage tMessage ) {
        List<String> tos = tMessage.getTo();
        String uid = tos.get(0);
        Channel channel = channelManager.getUserChannel(uid);
        if (channel == null) {
            log.info("用户{}目前不在线", uid);
        } else {
            channel.writeAndFlush(tMessage);
        }
    }
}
