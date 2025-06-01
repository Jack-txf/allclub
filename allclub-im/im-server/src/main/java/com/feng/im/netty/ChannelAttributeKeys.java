package com.feng.im.netty;

import io.netty.util.AttributeKey;

/**
 * @version 1.0
 * @Author txf
 * @Date 2025/5/6 11:17
 * @注释 认证ok之后，给channel附带一个用户ID，后面的处理器能够看到【已废弃】
 */
public class ChannelAttributeKeys {
    // 定义 AttributeKey（建议使用类名+具体用途命名）
    public static final AttributeKey<String> USER_ID = AttributeKey.valueOf("userId");
}
