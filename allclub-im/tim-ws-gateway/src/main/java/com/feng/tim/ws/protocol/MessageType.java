package com.feng.tim.ws.protocol;

/**
 * @Description: 消息类型
 * @Author: txf
 * @Date: 2025/10/11
 */
public class MessageType {
    // ping消息
    public static final String HEARTBEAT = "ping";
    // 聊天消息
    public static final String CHAT_TEXT = "chat_text";
    // 系统消息
    public static final String SYSTEM = "system";
    // 系统接收消息的时候出错了
    public static final String SYSTEM_ERROR = "system_error";


    public static final String IMAGE = "image";
    public static final String FILE = "file";
    public static final String NOTIFY = "notify";

    // public static final String te = 1; // 心跳
    // public static final byte PONG = 2; //
    // public static final byte CHAT = 3; // 普通的聊天消息
    // public static final byte System = 4; // 系统消息
    // public static final byte NOTIFY = 5; // 通知
}
