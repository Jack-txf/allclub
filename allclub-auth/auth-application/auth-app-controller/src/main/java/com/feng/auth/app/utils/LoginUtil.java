package com.feng.auth.app.utils;

import com.feng.auth.app.context.UserContextHolder;

public class LoginUtil {
    public static String getUserId() {
        return UserContextHolder.getLoginId();
    }
}
