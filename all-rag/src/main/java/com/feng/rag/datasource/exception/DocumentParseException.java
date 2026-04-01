package com.feng.rag.datasource.exception;

/**
 * 文档解析异常基类
 * <p>所有解析相关异常继承此类，便于全局异常处理器统一捕获。
 */
public class DocumentParseException extends RuntimeException {

    /** HTTP 状态码（方便全局异常处理器映射响应码） */
    private final int httpStatus;

    /** 错误码（便于前端/调用方程序化处理） */
    private final String errorCode;

    public DocumentParseException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public DocumentParseException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}


// ─────────────────────────────────────────────────────────────────────────────


/**
 * 不支持的文件类型异常
 * <p>当上传文件的 MIME 类型不在白名单时抛出。
 */
class UnsupportedFileTypeException extends DocumentParseException {

    public UnsupportedFileTypeException(String mimeType) {
        super(
            "UNSUPPORTED_FILE_TYPE",
            "不支持的文件类型：" + mimeType + "，请检查允许的 MIME 类型白名单",
            415  // HTTP 415 Unsupported Media Type
        );
    }
}


// ─────────────────────────────────────────────────────────────────────────────
/**
 * 文件大小超限异常
 */
class FileSizeExceededException extends DocumentParseException {

    public FileSizeExceededException(long actualBytes, long maxBytes) {
        super(
            "FILE_SIZE_EXCEEDED",
            String.format("文件大小 %d MB 超过最大限制 %d MB",
                actualBytes / 1024 / 1024, maxBytes / 1024 / 1024),
            413  // HTTP 413 Payload Too Large
        );
    }
}

// ─────────────────────────────────────────────────────────────────────────────
/**
 * 解析超时异常
 */
class ParseTimeoutException extends DocumentParseException {

    public ParseTimeoutException(String fileName, long timeoutSeconds) {
        super(
            "PARSE_TIMEOUT",
            String.format("文件 [%s] 解析超时（超过 %d 秒），请检查文件是否损坏或过于复杂",
                fileName, timeoutSeconds),
            408  // HTTP 408 Request Timeout
        );
    }
}