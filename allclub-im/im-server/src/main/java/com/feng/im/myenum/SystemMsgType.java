package com.feng.im.myenum;

/**
 * @version 1.0
 * @Author txf
 * @Date 2025/4/2 16:15
 * @注释 系统消息类型
 */
public enum SystemMsgType {
    /**
     * 登录
     */
    LOGIN(1, "login"),
    /**
     * 登出
     */
    LOGOUT(2, "logout"),
    /**
     * 断开连接
     */
    DISCONNECT(3, "disconnect"),
    /**
     * 断开连接
     */
    KICK(4, "kick");

    SystemMsgType(int i, String str) {
    }

    private int sysCode;
    private String str;
    public int getSysCode() {
        return sysCode;
    }

    public String getStr() {
        return str;
    }
}
