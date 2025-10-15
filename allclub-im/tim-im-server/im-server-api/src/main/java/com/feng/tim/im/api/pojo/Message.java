package com.feng.tim.im.api.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 消息表(Message)表实体类
 *
 * @author makejava
 * @since 2025-10-12 13:21:09
 */
@TableName("message")
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 消息唯一ID
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    // 所属会话ID
    private Long chatId;
    // 发送者ID
    private Long senderId;
    // 消息内容（文本/JSON，如图片消息存URL）
    private String content;
    // 消息类型（1-文本，2-图片，3-语音，4-文件）
    private Integer type;
    // 发送状态（0-发送中，1-已发送，2-发送失败）
    private Integer sendStatus;
    // 发送时间
    private Date sendTime;
    // 是否被发送者删除
    private Integer isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(Integer sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", senderId=" + senderId +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", sendStatus=" + sendStatus +
                ", sendTime=" + sendTime +
                ", isDeleted=" + isDeleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id) && Objects.equals(chatId, message.chatId)
                && Objects.equals(senderId, message.senderId) && Objects.equals(content, message.content)
                && Objects.equals(type, message.type) && Objects.equals(sendStatus, message.sendStatus)
                && Objects.equals(sendTime, message.sendTime) && Objects.equals(isDeleted, message.isDeleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, senderId, content, type, sendStatus, sendTime, isDeleted);
    }
}

