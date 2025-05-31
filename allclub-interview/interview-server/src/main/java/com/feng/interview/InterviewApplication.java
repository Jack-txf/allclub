package com.feng.interview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


// 面试模块
@SpringBootApplication
@ComponentScan("com.feng")
@MapperScan("com.feng.**.dao")
@EnableFeignClients(basePackages = "com.feng")
public class InterviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewApplication.class);
    }

}