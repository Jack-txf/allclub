package com.feng.im.myenum;

/**
 * @version 1.0
 * @Author txf
 * @Date 2025/4/2 16:14
 * @注释 聊天消息类型
 */
public enum ChatMsgType {
    TEXT(1, "text"),
    IMAGE(2, "image"),
    FILE(3, "file"),
    VOICE(4, "voice"),
    VIDEO(5, "video"),
    LOCATION(6, "location"),
    CARD(7, "card"),
    OTHER(8, "other");

    private int chatCode;
    private String str;


    ChatMsgType(int chatCode, String str) {
    }
    public int getChatCode() {
        return chatCode;
    }
    public String getStr() {
        return str;
    }
}
