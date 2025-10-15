package com.feng.tim.httpgateway.exception;

/**
 * @Description: 权限异常
 * @Author: txf
 * @Date: 2025/10/9
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
