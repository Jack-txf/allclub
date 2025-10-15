package com.feng.tim.httpgateway.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description: 白名单
 * @Author: txf
 * @Date: 2025/10/9
 */

@Setter
@Getter
@Component
@RefreshScope
@ConfigurationProperties(prefix = "gateway")
@Slf4j
/*
网关配置文件里面
gateway:
 whiteList:
   - /api/user/auth/login
   - /api/user/auth/register
   - .........
 */
public class WhiteUrlList {
    private List<String> whiteList;

    private Set<String> cache_matches = new HashSet<>();

    @PostConstruct
    public void doInit() {
        cache_matches.clear(); // 上次的缓存清除掉
        for (String w : whiteList) {
            cache_matches.add(w.replace("/**", ".*").replace("/*", "[^/]*"));
        }
        log.info("白名单配置文件加载完毕");
    }

    public boolean isWhite(String path) {
        return cache_matches.contains(path);
    }
}
