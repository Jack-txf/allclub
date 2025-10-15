package com.feng.tim.httpgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author Williams_Tian
 * @CreateDate 2025/10/6
 */
@SpringBootApplication
@EnableCaching
public class HttpGateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(HttpGateWayApplication.class);
    }
}