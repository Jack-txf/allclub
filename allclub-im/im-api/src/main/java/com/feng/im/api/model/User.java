package com.feng.im.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @Author txf
 * @Date 2025/5/7 15:59
 * @注释 user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String uid;
    private String name;
    private String username;
    private String password;
    private String email;
    private String token;
}
