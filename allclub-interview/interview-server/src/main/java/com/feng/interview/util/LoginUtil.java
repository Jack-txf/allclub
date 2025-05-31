package com.feng.interview.util;


import com.feng.interview.config.context.LoginContextHolder;

/**
 * 用户登录util
 *
 * @author: txf
 */
public class LoginUtil {

    public static String getLoginId() {
        return LoginContextHolder.getLoginId();
    }


}
