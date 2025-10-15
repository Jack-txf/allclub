package com.feng.tim.httpgateway.filter;

import com.feng.tim.httpgateway.config.WhiteUrlList;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Description: 登录过滤器
 * @Author: txf
 * @Date: 2025/10/9
 */
@Component
@Slf4j
public class LoginFilter implements GlobalFilter {

    @Resource
    private WhiteUrlList whiteUrlList; // 白名单

    private static final String TOKEN_NAME = "tim-token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /*
        解析请求，检查里面的token
         */
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutate = request.mutate();
        String url = request.getURI().getPath();
        // 从请求头里面拿到前后端约定的token，这里我们约定名字为tim-token
        HttpHeaders headers = request.getHeaders();
        String timToken = headers.getFirst(TOKEN_NAME);
        log.info("【网关token】{}", timToken);
        log.info("【gateway LoginFilter.filter过滤器 url：】:{}", url);
        // 如果是白名单的话，直接放行
        if ( whiteUrlList.isWhite(url)) {
            return chain.filter(exchange);
        }
        // 不是白名单---就需要登录认证授权那些的了
        else {
            // 1.将此token解析为uid
            String uid = "1";
            log.info("【网关satoken解析 uid】{}", uid);
            mutate.header("uidStr", uid); // 请求头里面加上这个
            return chain.filter(exchange.mutate()
                    .request(mutate.build())
                    .build());
        }
    }
}
