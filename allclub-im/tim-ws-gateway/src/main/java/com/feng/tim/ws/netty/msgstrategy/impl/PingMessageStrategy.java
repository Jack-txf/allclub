package com.feng.tim.ws.netty.msgstrategy.impl;

import com.feng.tim.ws.netty.msgstrategy.MessageStrategy;
import com.feng.tim.ws.protocol.MessageType;
import com.feng.tim.ws.protocol.TMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: ping消息处理
 * @Author: txf
 * @Date: 2025/10/12
 */
@Component
@Slf4j
public class PingMessageStrategy extends MessageStrategy {

    public PingMessageStrategy() {
        super(MessageType.HEARTBEAT);
    }
    @Override
    public void tackle(TMessage tMessage) {

    }
}
