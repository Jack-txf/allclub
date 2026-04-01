package com.feng.rag.vector.exception;

import lombok.Getter;

/**
 * Milvus 操作异常
 *
 * @author txf
 * @since 2026/3/26
 */
@Getter
public class MilvusException extends RuntimeException {

    private final String operation;

    public MilvusException(String message) {
        super(message);
        this.operation = "UNKNOWN";
    }

    public MilvusException(String message, Throwable cause) {
        super(message, cause);
        this.operation = "UNKNOWN";
    }

    public MilvusException(String operation, String message) {
        super(String.format("[%s] %s", operation, message));
        this.operation = operation;
    }

    public MilvusException(String operation, String message, Throwable cause) {
        super(String.format("[%s] %s", operation, message), cause);
        this.operation = operation;
    }

}
