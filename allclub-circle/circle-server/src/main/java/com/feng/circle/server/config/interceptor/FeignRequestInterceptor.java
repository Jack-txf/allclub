package com.feng.circle.server.config.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * Feign请求拦截器
 * @author: txf
 * @date: 2023/12/3
 */
/*
 * 为什么需要这个？
 * 流程1：网关 -> 微服务1。没有微服务之间的调用
 *      网关层有拦截器，将id写入header，这个请求进入微服务1，微服务1的拦截器可以检测到发来请求中的header情况
 * 流程2：网关 -> 微服务1 --> 微服务2。有微服务之间的调用
 *      网关层有拦截器，将id写入header，这个请求进入微服务1，微服务1的拦截器可以检测到发来请求中的header情况；
 *    但是微服务1与微服务2之间是用openfeign实现远程调用的，请求头里面并不会携带token，微服务2的拦截器找不到这个请求的header里面到底是啥情况。
 *    故需要这个。
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        if (Objects.nonNull(request)) { //对象不空
            String loginId = request.getHeader("loginId");
            if (StringUtils.isNotBlank(loginId)) {
                requestTemplate.header("loginId", loginId);
            }
        }
    }

}
