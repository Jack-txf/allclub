package com.feng.tim.ws.netty.msgstrategy.impl;

import com.feng.tim.ws.netty.msgstrategy.MessageStrategy;
import com.feng.tim.ws.protocol.MessageType;
import com.feng.tim.ws.protocol.TMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: 系统错误消息处理器
 * @Author: txf
 * @Date: 2025/10/12
 */
@Component
@Slf4j
public class SystemErrorStrategy extends MessageStrategy {
    public SystemErrorStrategy() {
        super(MessageType.SYSTEM_ERROR);
    }

    @Override
    public void tackle(TMessage tMessage) {
        log.info("处理系统错误消息..., {}", tMessage);
    }
}
