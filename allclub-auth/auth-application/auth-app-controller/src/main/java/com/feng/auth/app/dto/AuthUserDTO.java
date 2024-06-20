package com.feng.auth.app.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用戶信息dto TODO 这个原本是在auth-api层里面的
 *
 * @author: txf
 * @date: 2023/11/1
 */
@Data
public class AuthUserDTO implements Serializable {

    private Long id;

    private String userName;

    private String nickName;

    private String email;

    private String phone;

    private String password;

    private Integer sex;

    private String avatar;

    private Integer status;

    private String introduce;

    private String extJson;

}

