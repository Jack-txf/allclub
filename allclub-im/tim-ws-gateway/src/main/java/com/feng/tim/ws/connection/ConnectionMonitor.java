package com.feng.tim.ws.connection;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: ws-ConnectionManager里面的连接监视器 -- 清除无用的连接
 * @Author: txf
 * @Date: 2025/11/12
 */
@Slf4j
@Component
public class ConnectionMonitor {
    @Resource
    private ConnectionManager connectionManager;

    private final ScheduledThreadPoolExecutor scheduleTimer;

    public ConnectionMonitor() {
        scheduleTimer = new ScheduledThreadPoolExecutor(1);
    }
    @PostConstruct
    public void start() {
        log.info("连接监视器启动~~");
        scheduleTimer.scheduleAtFixedRate(this::checkLocalNodeConnections,
                0, 60, TimeUnit.SECONDS);
    }

    // 定时任务检查本地结点的连接是否ok
    public void checkLocalNodeConnections() {
        Collection<ConnectionInfo> connections = connectionManager.getAllConnections();
        Instant now = Instant.now();
        for ( ConnectionInfo info : connections ) {
            if ( info.getChannel() == null ) {
                log.info("本地结点连接被关闭，清除无用的连接：{}", info);
                connectionManager.remove(info);
            }
            if ( info.getLastActiveTime().plusSeconds(60L).isBefore(now) ) {
                // 超过60秒没有往来消息了,重试次数+1
                info.incrementRetryCount();
            }
            if ( info.getRetryCount() > 3 ) {
                log.info("本地长时间无响应，清除无用的连接：{}", info);
                connectionManager.remove(info);
            }
        }
    }
}
