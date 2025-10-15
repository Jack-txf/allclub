package com.feng.tim.im;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Williams_Tian
 *
 * @CreateDate 2025/10/6
 */
// IM业务服务
@SpringBootApplication
@MapperScan("com.feng.tim.im.mapper")
@EnableDubbo
public class IMApplication {
    public static void main(String[] args) {
        SpringApplication.run(IMApplication.class, args);
    }
}
