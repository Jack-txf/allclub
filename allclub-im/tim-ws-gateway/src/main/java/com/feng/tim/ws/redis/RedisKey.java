package com.feng.tim.ws.redis;

import com.feng.tim.ws.exception.RedisKeyBuildException;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: redis的key
 * @Author: txf
 * @Date: 2025/10/11
 */
@Slf4j
public class RedisKey {
    public static final String USER_TOKEN = "tim-token:";
    public static final String WS_CENTER = "ws-center:";
    public static String userTokenKey( long uid ) {
        return USER_TOKEN + (uid % 10);
    }

    public static String wsCenterKey( String uid ) {
        try {
            return WS_CENTER + (Long.parseLong(uid) % 20);
        } catch ( NumberFormatException e ) {
            log.error("uid转换异常: {}", e.getMessage());
            throw new RedisKeyBuildException("uid转换异常" + e.getMessage());
        }
    }
}
