package com.feng.tim.ws.netty.msgstrategy;

import com.feng.tim.ws.protocol.TMessage;

/**
 * @Description: 消息处理接口
 * @Author: txf
 * @Date: 2025/10/12
 */
public abstract class MessageStrategy {
    private final String type;
    public MessageStrategy(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public abstract void tackle(TMessage tMessage);
}
