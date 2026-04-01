package com.feng.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description: All-rag主启动类
 * @Author: txf
 * @Date: 2026/3/23
 */
@Slf4j
@SpringBootApplication
public class RagApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
        log.info("================================================");
        log.info("  RAG 模块启动成功");
        log.info("================================================");
    }
}







