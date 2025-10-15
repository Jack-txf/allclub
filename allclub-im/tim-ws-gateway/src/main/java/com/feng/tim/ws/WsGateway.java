package com.feng.tim.ws;

import com.feng.tim.ws.netty.TimBootstrap;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// ws长连接网关
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@Slf4j
@EnableDubbo // 启动Dubbo
public class WsGateway implements CommandLineRunner {

    @Resource
    private TimBootstrap timBootstrap;

    public static void main(String[] args) {
        SpringApplication.run(WsGateway.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        log.info("ws网关启动成功中....");
        // Netty网关启动...不阻塞主线程，但是有dubbo，所以即时是没有tomcat容器，应用还是不会结束
        new Thread(() -> timBootstrap.start()).start();
    }
}
