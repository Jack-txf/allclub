package com.feng.gateway.filter;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关层：登录拦截器
 *
 * @author: txf
 * @date: 2023/11/26
 */
@Component
@Slf4j
public class LoginFilter implements GlobalFilter {

    @Override
    @SneakyThrows
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /*
        解析请求，检查里面的token
         */
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //ServerHttpRequest.Builder是这种类型哦
        ServerHttpRequest.Builder mutate = request.mutate();

        String url = request.getURI().getPath();
        log.info("LoginFilter.filter.url:{}", url);
        if (url.equals("/user/doLogin")) { // 是登录请求，直接放行了
            return chain.filter(exchange);
        }
        // 获取当前会话的token
        /*
          token具体形式为：   satoken: "txf xxxx-xxxx-xxxx-xxxx" 有txf的前缀
          也就是说前端只需要按这种格式发送请求的时候携带这种token，sa-token框架就会自动帮你从header里面提取出token了
         */
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        log.info("LoginFilter.filter.url:{}", new Gson().toJson(tokenInfo));
        String loginId = (String) tokenInfo.getLoginId(); // 得到用户的openID，因为是用微信公众号验证码登录的
        mutate.header("loginId", loginId); // 请求头里面加上这个
        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }

}
