package com.feng.tim.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Williams_Tian
 * @CreateDate 2025/10/6 2025/10/6
 */
@SpringBootApplication
@MapperScan("com.feng.tim.auth.mapper")
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
