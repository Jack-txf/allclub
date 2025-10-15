package com.feng.tim.im.api.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 会话-用户关联表(SessionUser)表实体类
 *
 * @author makejava
 * @since 2025-10-12 13:21:09
 */
@TableName("session_user")
public class SessionUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    // 会话ID
    private Long chatId;
    // 用户ID
    private Long userId;
    // 加入时间
    private Date joinTime;
    // 群聊角色（0-普通成员，1-管理员，2-群主；单聊忽略）
    private Integer role;
    // 未读消息数
    private Integer unreadCount;
    // 是否静音（0-否，1-是）
    private Integer isMuted;
    // 用户是否删除该会话
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Date joinTime) {
        this.joinTime = joinTime;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Integer isMuted) {
        this.isMuted = isMuted;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "SessionUser{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", userId=" + userId +
                ", joinTime=" + joinTime +
                ", role=" + role +
                ", unreadCount=" + unreadCount +
                ", isMuted=" + isMuted +
                ", isDeleted=" + isDeleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SessionUser that = (SessionUser) o;
        return Objects.equals(id, that.id) && Objects.equals(chatId, that.chatId)
                && Objects.equals(userId, that.userId) && Objects.equals(joinTime, that.joinTime)
                && Objects.equals(role, that.role) && Objects.equals(unreadCount, that.unreadCount)
                && Objects.equals(isMuted, that.isMuted) && Objects.equals(isDeleted, that.isDeleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, userId, joinTime, role, unreadCount, isMuted, isDeleted);
    }
}

