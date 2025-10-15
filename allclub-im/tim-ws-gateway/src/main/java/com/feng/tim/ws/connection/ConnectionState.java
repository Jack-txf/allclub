package com.feng.tim.ws.connection;

import lombok.Getter;

/**
 * 连接状态枚举（状态流转：INIT -> CONNECTING -> CONNECTED -> DISCONNECTED）
 */
@Getter
public enum ConnectionState {

    // Local("本地"),
    // Remote("其他网关"),
    // Offline("离线"),

    INIT("初始状态"),
    CONNECTING("连接中"),
    CONNECTED("已连接"),
    DISCONNECTED("已断开"),
    RECONNECTING("重连中"),
    CLOSED("已关闭"); // 最终状态，不可恢复


    private final String desc;
    ConnectionState(String desc) {
        this.desc = desc;
    }
}
