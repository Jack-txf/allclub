package com.feng.rag.datasource.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RAG 数据源模块配置属性
 *
 * <p>所有配置项均映射自 application.yml 的 rag.datasource 前缀。
 * 修改配置无需重新编译，应用重启后生效。
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag.datasource")
public class DataSourceProperties {

    /** 解析器相关配置 */
    private Parser parser = new Parser();

    /** 文件类型白名单（MIME type 列表） */
    private List<String> allowedMimeTypes = List.of(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-excel",
        "text/plain",
        "text/html",
        "text/markdown"
    );

    @Data
    public static class Parser {
        /** 解析超时时间（秒），默认 60 秒 */
        private int parseTimeoutSeconds = 60;
        /** 单文件最大字节数，默认 200MB */
        private long maxFileSizeBytes = 200L * 1024 * 1024;
        /** 是否提取文档元数据，默认 true */
        private boolean extractMetadata = true;
        /** 最大提取文本长度（字符数），默认 500 万字符 */
        private int maxContentLength = 5_000_000;
        /** OCR 配置 */
        private Ocr ocr = new Ocr();
    }

    @Data
    public static class Ocr {
        /** 是否启用 OCR（需要安装 Tesseract），默认 false */
        private boolean enabled = false;
        /** Tesseract 识别语言，默认中文简体 + 英文 */
        private String language = "chi_sim+eng";
    }
}