package com.feng.im.netty;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: netty_channel管理器
 * @Author: txf
 * @Date: 2025/5/5
 */
@Slf4j
@Component
public class ChannelManager {
    private final ConcurrentHashMap<String, Channel> userChannels = new ConcurrentHashMap<>();

    // 1.用户登录
    public void addUserChannel(String userId, Channel channel) {
        if (userChannels.containsKey(userId)) // 已经连接过了
            return;
        userChannels.put(userId, channel);
        print();
    }

    // 2.得到用户的channel
    public Channel getUserChannel(String userId) {
        return userChannels.get(userId);
    }

    // 3.用户退出连接
    public void removeUserChannel(String userId) {
        Channel channel = userChannels.remove(userId);
        if (channel != null) channel.close(); // 关闭
        print();
    }
    public void removeUserChannel(Channel channel) {
        for (String userId : userChannels.keySet()) {
            if (userChannels.get(userId) == channel) {
                userChannels.remove(userId);
                break;
            }
        }
        if (channel != null) channel.close(); // 关闭
        print();
    }

    private void print() {
        log.info("=========");
        for (String userId : userChannels.keySet()) {
            log.info("{} : {}",  userId, userChannels.get(userId));
        }
        log.info("=========");
    }
}
