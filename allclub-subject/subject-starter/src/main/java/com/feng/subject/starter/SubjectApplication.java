package com.feng.subject.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/*
刷题微服务的启动类
 */
@SpringBootApplication
@ComponentScan("com.feng.subject")
@MapperScan("com.feng.**.mapper")
public class SubjectApplication {

    /*
    druid数据源报这个警告信息：discard long time none received connection. , jdbcUrl :.......
    加上这个静态代码块就不会报这个了
     */
    static {
        System.setProperty("druid.mysql.usePingMethod","false");
    }
    public static void main(String[] args) {
        SpringApplication.run(SubjectApplication.class, args);
    }
}
