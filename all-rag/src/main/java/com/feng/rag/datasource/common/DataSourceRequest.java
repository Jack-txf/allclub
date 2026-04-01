package com.feng.rag.datasource.common;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 统一数据源接入请求模型
 *
 * <p>支持三种接入方式：
 * <ol>
 *   <li><b>文件上传</b>：通过 multipart/form-data 上传本地文件</li>
 *   <li><b>URL 抓取</b>：提供 HTTP/HTTPS URL，系统自动下载并解析</li>
 *   <li><b>原始文本</b>：直接提交文本内容（纯文本场景）</li>
 * </ol>
 *
 * <p>三种方式互斥，优先级：file > url > rawText
 */
@Data
public class DataSourceRequest {

    /** 数据源类型 */
    @NotNull(message = "数据源类型不能为空")
    private SourceType sourceType;

    // ─────────── 方式一：文件上传 ───────────
    /** 上传的文件（multipart 方式时由 Controller 注入，非 JSON 字段） */
    private MultipartFile file;

    // ─────────── 方式二：URL 抓取 ───────────
    /** 文档 URL，支持 http/https 协议 */
    private String url;

    // ─────────── 方式三：原始文本 ───────────
    /** 纯文本内容，直接作为解析结果的 content */
    private String rawText;

    /** rawText 的文件名（用于元数据记录） */
    private String rawTextFileName;

    // ─────────── 公共选项 ───────────
    /**
     * 业务来源标识，由调用方传入，原样透传到解析结果的 sourceId。
     * 建议格式：{系统名}_{业务ID}，如 "crm_contract_10086"。
     */
    private String sourceId;

    /**
     * 是否强制提取元数据（覆盖全局配置）。
     * null 表示使用全局配置。
     */
    private Boolean extractMetadata;

    /**
     * 数据源类型枚举
     */
    public enum SourceType {
        /** 本地文件上传 */
        FILE_UPLOAD,
        /** 远程 URL 抓取 */
        URL,
        /** 原始文本直接提交 */
        RAW_TEXT
    }
}