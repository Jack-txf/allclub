package com.feng.im.myenum;

import lombok.Getter;

/**
 * @version 1.0
 * @Author txf
 * @Date 2025/5/6 15:24
 * @注释 消息类型
 */
public enum MsgType {
    SYSTEM("系统消息", "1"),
    CHAT("文本消息", "2");

    @Getter
    private final String type;

    @Getter
    private final String index;

    MsgType(String type, String index) {
        this.type = type;
        this.index = index;
    }

}
