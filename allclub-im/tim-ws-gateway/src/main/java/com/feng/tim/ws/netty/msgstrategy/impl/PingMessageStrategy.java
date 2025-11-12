package com.feng.tim.ws.netty.msgstrategy.impl;

import com.feng.tim.ws.connection.ConnectionInfo;
import com.feng.tim.ws.connection.ConnectionManager;
import com.feng.tim.ws.netty.msgstrategy.MessageStrategy;
import com.feng.tim.ws.protocol.MessageType;
import com.feng.tim.ws.protocol.TMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: ping消息处理 -- 应用层的心跳处理
 * @Author: txf
 * @Date: 2025/10/12
 */
@Component
@Slf4j
public class PingMessageStrategy extends MessageStrategy {
    @Resource
    private ConnectionManager connectionManager;

    public PingMessageStrategy() {
        super(MessageType.HEARTBEAT);
    }
    @Override
    public void tackle(TMessage tMessage) {
        log.info("系统收到该用户的心跳消息..., {}", tMessage);
        String fromUser = tMessage.getFromUser();
        ConnectionInfo connectionInfo = connectionManager.getByUserId(fromUser);
        if (connectionInfo != null) {
            connectionInfo.updateLastActiveTime();
        }
    }
}
