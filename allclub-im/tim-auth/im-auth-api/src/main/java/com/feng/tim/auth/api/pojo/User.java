package com.feng.tim.auth.api.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tim_user")
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = -79562238115133802L;
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long uid;
    /**
     * 用户账户
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date modifyTime;
    /**
     * 上次登录时间
     */
    private Date lastLogin;
}

