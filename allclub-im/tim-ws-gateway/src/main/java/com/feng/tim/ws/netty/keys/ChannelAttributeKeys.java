package com.feng.tim.ws.netty.keys;

import io.netty.util.AttributeKey;

/**
 * @Description: keys
 * @Author: txf
 * @Date: 2025/10/5
 */
public class ChannelAttributeKeys {
    // 定义 AttributeKey
    public static final AttributeKey<String> USERNAME = AttributeKey.valueOf("username");
    public static final AttributeKey<Long> UID = AttributeKey.valueOf("uid");
}
