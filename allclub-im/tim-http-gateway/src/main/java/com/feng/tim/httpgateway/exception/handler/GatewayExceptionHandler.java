package com.feng.tim.httpgateway.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.tim.httpgateway.common.R;
import com.feng.tim.httpgateway.exception.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Description: 网关异常处理器
 * @Author: txf
 * @Date: 2025/10/9
 */
@Component
@Slf4j
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 得到请求 相应对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 处理
        int code;
        String message = "";
        if ( ex instanceof AuthException ) {
            code = 401;
            message = "用户无权限";
            log.error("出现权限异常 {}", ex.getMessage());
        } else {
            code = 500;
            message = "系统繁忙";
            log.error("系统繁忙,请稍后再试. {}", ex.getMessage());
        }
        R fail = R.fail(code, message);
        // 写回响应
        // 设置为json格式
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            byte[] bytes = null;
            try {
                bytes = objectMapper.writeValueAsBytes(fail);
            } catch (JsonProcessingException e) {
               log.error("字节序列化错误, {}",e.getMessage());
            }
            assert bytes != null;
            return dataBufferFactory.wrap(bytes);
        }));
    }
}
