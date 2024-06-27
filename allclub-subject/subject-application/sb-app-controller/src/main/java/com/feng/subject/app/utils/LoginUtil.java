package com.feng.subject.app.utils;

import com.feng.subject.app.context.UserContextHolder;

public class LoginUtil {
    public static String getUserId() {
        return UserContextHolder.getLoginId();
    }
}
