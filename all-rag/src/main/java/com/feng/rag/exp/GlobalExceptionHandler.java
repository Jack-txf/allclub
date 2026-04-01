package com.feng.rag.exp;

import com.feng.rag.controller.DataSourceController;
import com.feng.rag.datasource.exception.DocumentParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（文件类型不支持、大小超限、解析超时等）。
     */
    @ExceptionHandler(DocumentParseException.class)
    public ResponseEntity<DataSourceController.ApiResponse<Void>> handleDocumentParseException(DocumentParseException e) {
        log.warn("[ExceptionHandler] 业务异常: errorCode={}, message={}", e.getErrorCode(), e.getMessage());

        return ResponseEntity
            .status(e.getHttpStatus())
            .body(DataSourceController.ApiResponse.error("[" + e.getErrorCode() + "] " + e.getMessage()));
    }

    /**
     * 处理 Spring 的文件大小超限异常（application.yml 中 max-file-size 的限制层）。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<DataSourceController.ApiResponse<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("[ExceptionHandler] 文件上传大小超过 Spring 限制: {}", e.getMessage());

        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(DataSourceController.ApiResponse.error("上传文件大小超过系统限制，请检查文件大小配置"));
    }

    /**
     * 处理请求参数校验失败（@Valid 注解触发）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DataSourceController.ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));

        log.warn("[ExceptionHandler] 请求参数校验失败: {}", errorMessage);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(DataSourceController.ApiResponse.error("请求参数错误：" + errorMessage));
    }

    /**
     * 兜底处理：所有未预期的异常。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataSourceController.ApiResponse<Void>> handleUnexpectedException(Exception e) {
        log.error("[ExceptionHandler] 未预期异常，请检查日志排查根因", e);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(DataSourceController.ApiResponse.error("服务内部错误，请联系管理员。错误类型：" + e.getClass().getSimpleName()));
    }
}