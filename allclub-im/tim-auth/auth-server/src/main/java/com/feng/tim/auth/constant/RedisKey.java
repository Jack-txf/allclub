package com.feng.tim.auth.constant;

/**
 * @Description: redis的key
 * @Author: txf
 * @Date: 2025/10/11
 */
public class RedisKey {
    public static final String USER_TOKEN = "tim-token:";
    public static String userTokenKey( long uid ) {
        return USER_TOKEN + (uid % 10);
    }
}
