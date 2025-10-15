package com.feng.tim.ws.netty.msgstrategy.impl;

import com.feng.tim.ws.connection.ConnectionInfo;
import com.feng.tim.ws.connection.ConnectionManager;
import com.feng.tim.ws.connection.UserNodeState;
import com.feng.tim.ws.netty.msgstrategy.MessageStrategy;
import com.feng.tim.ws.protocol.ChatType;
import com.feng.tim.ws.protocol.MessageType;
import com.feng.tim.ws.protocol.TMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: 聊天消息处理策略
 * @Author: txf
 * @Date: 2025/10/12
 */
@Component
@Slf4j
public class ChatMessageStrategy extends MessageStrategy {
    @Resource
    private ConnectionManager connectionManager;

    public ChatMessageStrategy() {
        super(MessageType.CHAT_TEXT);
    }
    // 处理聊天类型的消息
    @Override
    public void tackle(TMessage tMessage) {
        String chatType = tMessage.getChatType();
        log.info("处理聊天类型的消息..., {}", tMessage);
        if (ChatType.SINGLE.equals(chatType)) { // 点对点聊天
            // TODO 1.调用业务处理完成消息存储判断等等  2.根据调用结果确定是否推送消息给toUser

            String toUser = tMessage.getToUser();
            // toUser的在线状态
            String state = connectionManager.getUserNodeState(toUser);
            if (UserNodeState.Local.equals(state)) { // 本地在线，推送给toUser
                ConnectionInfo connect = connectionManager.getByUserId(toUser);
                connect.getChannel().writeAndFlush(tMessage);
            }
            else if ( !UserNodeState.Offline.equals(state)) { // 其他网关节点在线，通过RocketMQ推送给toUser

            } else { // 对方离线，保存消息

            }
        }
        else if (ChatType.GROUP.equals(chatType)) { // 群聊业务
            // TODO 群组消息待做
        }
        else { // 默认处理系统消息

        }
    }
}
