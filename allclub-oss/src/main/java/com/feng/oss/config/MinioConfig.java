package com.feng.oss.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * minio 配置管理
 * @author Williams_Tian
 * @CreateDate 2024/6/18
 */
@Configuration
public class MinioConfig {
    @Value("${minio.url}")
    private String url;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String accessSecret;

    /*
     * 构造minioClient
     */
    @Bean("minioClient")
    public MinioClient getMinioClient() {
        return MinioClient.builder().endpoint(url).credentials(accessKey, accessSecret).build();
    }
}
