package com.feng.tim.ws.exception;

/**
 * @Description: 网络异常
 * @Author: txf
 * @Date: 2025/10/12
 */
public class NetworkException extends RuntimeException{

    public NetworkException() {
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }
}
