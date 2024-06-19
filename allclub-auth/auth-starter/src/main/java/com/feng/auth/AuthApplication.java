package com.feng.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Williams_Tian
 * @CreateDate 2024/6/19
 */
@SpringBootApplication
@ComponentScan("com.feng")
@MapperScan("com.feng.**.mapper")
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
