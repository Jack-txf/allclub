package com.feng.tim.ws.protocol;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: 消息
 * @Author: txf
 * @Date: 2025/10/11
 */
public class TMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一ID
     */
    private String msgId;

    /**
     * 消息类型：text/image/system/...
     */
    private String type;

    /**
     * 聊天类型：single / group
     */
    private String chatType;

    /**
     * 发送方ID
     */
    private String fromUser;

    /**
     * 接收方ID（单聊时是用户ID，群聊时是群ID）
     */
    private String toUser;

    /**
     * 消息内容（文本内容或图片URL）
     */
    private String content;

    /**
     * 扩展字段：可存放图片宽高、文件名等
     */
    private Map<String, Object> extra;

    /**
     * 消息发送时间戳（毫秒）
     */
    private long timestamp;

    public TMessage() {
    }

    public TMessage(String msgId, String type, String chatType, String fromUser,
                    String toUser, String content, Map<String, Object> extra,
                    long timestamp) {
        this.type = type;
        this.msgId = msgId;
        this.chatType = chatType;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.extra = extra;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "TMessage{" +
                "msgId='" + msgId + '\'' +
                ", type='" + type + '\'' +
                ", chatType='" + chatType + '\'' +
                ", fromUser='" + fromUser + '\'' +
                ", toUser='" + toUser + '\'' +
                ", content='" + content + '\'' +
                ", extra=" + extra +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TMessage tMessage = (TMessage) o;
        return timestamp == tMessage.timestamp && Objects.equals(msgId, tMessage.msgId)
                && Objects.equals(type, tMessage.type) && Objects.equals(chatType, tMessage.chatType)
                && Objects.equals(fromUser, tMessage.fromUser) && Objects.equals(toUser, tMessage.toUser)
                && Objects.equals(content, tMessage.content) && Objects.equals(extra, tMessage.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId, type, chatType, fromUser, toUser, content, extra, timestamp);
    }

    // 错误消息
    public static TMessage error(String msgId, String content) {
        return new TMessage(msgId, MessageType.SYSTEM_ERROR, ChatType.SYSTEM, "system-tim",
                null, content, null, System.currentTimeMillis());
    }

    // 心跳消息
    public static TMessage heartbeat() {
        String msgId = "heartbeat-" + System.currentTimeMillis();
        return new TMessage(msgId, MessageType.HEARTBEAT, ChatType.SYSTEM, "system-tim",
                null, "ping", null, System.currentTimeMillis());
    }
}
