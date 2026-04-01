package com.feng.rag.model.exception;

/**
 * @Description: 不支持的提供商
 * @Author: txf
 * @Date: 2026/3/24
 */
public class NotSupportProvider extends RuntimeException {
    public NotSupportProvider(String message) {
        super(message);
    }
}
