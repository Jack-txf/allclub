package com.feng.subject.common.utils;

import com.feng.subject.common.context.UserContextHolder;

public class LoginUtil {
    public static String getUserId() {
        return UserContextHolder.getLoginId();
    }
}
