package com.feng.circle.server.util;

import com.feng.circle.server.config.context.UserContextHolder;

/**
 * 用户登录util
 *
 * @author: txf
 * @date: 2023/11/26
 */
public class LoginUtil {

    public static String getLoginId() {
        return UserContextHolder.getLoginId();
    }


}
