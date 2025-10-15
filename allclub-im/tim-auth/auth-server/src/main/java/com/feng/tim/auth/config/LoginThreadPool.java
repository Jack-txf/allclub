package com.feng.tim.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/10/9
 */
@Configuration
public class LoginThreadPool {
    @Bean("timLoginThreadPool")
    public ThreadPoolExecutor timLoginThreadPool() {
        return new ThreadPoolExecutor(5, 10, 1000L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(100),
                new LoginThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private static class LoginThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            String namePrefix = "login-thread-";
            t.setName(namePrefix + poolNumber.getAndIncrement());
            return t;
        }
    }
}
