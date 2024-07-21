package com.feng.circle.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.feng.**.dao")
@ComponentScan("com.feng")
@EnableFeignClients(basePackages = "com.feng")
public class CircleApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircleApplication.class, args);
    }
}
