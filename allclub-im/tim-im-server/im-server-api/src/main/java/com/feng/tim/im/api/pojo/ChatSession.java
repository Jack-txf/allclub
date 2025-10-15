package com.feng.tim.im.api.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 会话表(ChatSession)表实体类
 *
 * @author makejava
 * @since 2025-10-12 13:21:03
 */
@TableName("chat_session")
public class ChatSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 会话唯一ID
    @TableId(value = "chat_id", type = IdType.AUTO)
    private Long chatId;
    // 会话类型（1-单聊，2-群聊）
    private Integer type;
    // 会话名称（群聊必填，单聊可为空）
    private String name;
    // 会话头像（群聊用）
    private String avatar;
    // 创建者ID（群聊必填）[ 单聊的话就是好友申请发起者 ]
    private Long creatorId;
    // 最后一条消息ID
    private Long lastMsgId;
    // 最后一条消息时间
    private Date lastMsgTime;
    // 是否删除（0-否，1-是）
    private Integer isDeleted;

    private Date createdAt;

    private Date updatedAt;

    public ChatSession(Long chatId, Integer type, String name, String avatar,
                       Long creatorId, Long lastMsgId, Date lastMsgTime, Date updatedAt,
                       Integer isDeleted, Date createdAt) {
        this.chatId = chatId;
        this.type = type;
        this.name = name;
        this.avatar = avatar;
        this.creatorId = creatorId;
        this.lastMsgId = lastMsgId;
        this.lastMsgTime = lastMsgTime;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(Long lastMsgId) {
        this.lastMsgId = lastMsgId;
    }

    public Date getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(Date lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ChatSession{" +
                "chatId=" + chatId +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", creatorId=" + creatorId +
                ", lastMsgId=" + lastMsgId +
                ", lastMsgTime=" + lastMsgTime +
                ", isDeleted=" + isDeleted +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChatSession that = (ChatSession) o;
        return Objects.equals(chatId, that.chatId) &&
                Objects.equals(type, that.type) && Objects.equals(name, that.name)
                && Objects.equals(avatar, that.avatar) &&
                Objects.equals(creatorId, that.creatorId) &&
                Objects.equals(lastMsgId, that.lastMsgId) &&
                Objects.equals(lastMsgTime, that.lastMsgTime) &&
                Objects.equals(isDeleted, that.isDeleted) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, type, name, avatar,
                creatorId, lastMsgId, lastMsgTime, isDeleted, createdAt, updatedAt);
    }
}

