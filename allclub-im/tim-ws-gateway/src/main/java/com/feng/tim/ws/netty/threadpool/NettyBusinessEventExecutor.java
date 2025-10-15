package com.feng.tim.ws.netty.threadpool;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.RejectedExecutionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description: Netty业务处理的线程池配置
 * @Author: txf
 * @Date: 2025/10/12
 */
@Configuration
@Slf4j
public class NettyBusinessEventExecutor {

    // 1.握手拦截器handler在用, 2.链接感知器handler在用
    @Bean("businessEventExecutor1")
    public DefaultEventExecutorGroup businessEventExecutor() {
        // 8：线程数
        // 10000：阻塞队列最大长度
        // RejectedExecutionHandler：任务拒绝策略
        return new DefaultEventExecutorGroup(
                8,
                ( r ) -> {
                    Thread thread = new Thread(r);
                    thread.setName("business-thread-" + thread.getId());
                    return thread;
                },
                10000,
                (r, e) -> {
                    log.error("任务队列已满咯~~~ {}, size: {}", r.toString(), e.pendingTasks());
                }
        );
    }

    // 路由业务线程池  3.消息路由处理handler在用
    @Bean("routeEventExecutor1")
    public DefaultEventExecutorGroup routeEventExecutor1() {
        return new DefaultEventExecutorGroup(
                8,
                ( r ) -> {
                    Thread thread = new Thread(r);
                    thread.setName("business-Route-thread-" + thread.getId());
                    return thread;
                },
                10000,
                (r, e) -> {
                    log.error("任务队列已满咯~~~ {}, size: {}", r.toString(), e.pendingTasks());
                }
        );
    }
}
