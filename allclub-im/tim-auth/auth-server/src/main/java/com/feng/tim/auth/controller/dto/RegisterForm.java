package com.feng.tim.auth.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/10/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterForm {
    private String username;
    private String nickname;
    private String password;
    private String retryPwd;
}
