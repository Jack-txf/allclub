package com.feng.tim.im.api.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * (FriendRelated)表实体类
 *
 * @author makejava
 * @since 2025-10-12 13:21:09
 */
@TableName("friend_related")
public class FriendRelated implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 好友关系id
    @TableId(type = IdType.AUTO)
    private Long friendRelatedId;
    // 用户aid
    private Long userAid;
    // 用户bid
    private Long userBid;
    // 创建时间
    private Date createTime;


    public Long getFriendRelatedId() {
        return friendRelatedId;
    }

    public void setFriendRelatedId(Long friendRelatedId) {
        this.friendRelatedId = friendRelatedId;
    }

    public Long getUserAid() {
        return userAid;
    }

    public void setUserAid(Long userAid) {
        this.userAid = userAid;
    }

    public Long getUserBid() {
        return userBid;
    }

    public void setUserBid(Long userBid) {
        this.userBid = userBid;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "FriendRelated{" +
                "friendRelatedId=" + friendRelatedId +
                ", userAid=" + userAid +
                ", userBid=" + userBid +
                ", createTime=" + createTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FriendRelated that = (FriendRelated) o;
        return Objects.equals(friendRelatedId, that.friendRelatedId)
                && Objects.equals(userAid, that.userAid) &&
                Objects.equals(userBid, that.userBid) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendRelatedId, userAid, userBid, createTime);
    }
}

