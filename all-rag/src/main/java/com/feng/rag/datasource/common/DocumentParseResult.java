package com.feng.rag.datasource.common;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 文档解析结果 —— 统一输出模型
 *
 * <p>无论输入是 PDF、Word、PPT 还是 TXT，所有解析器最终都产出此对象。
 * 下游的 Chunking / Embedding 模块只需依赖本模型，无需关心原始格式。
 *
 * <p>生产注意事项：
 * <ul>
 *   <li>content 可能非常大（数 MB），序列化到 HTTP 响应时建议流式输出或分页</li>
 *   <li>metadata 中的值全部为 String，调用方自行按需转换类型</li>
 *   <li>parseErrors 非空表示解析部分失败，content 仍可能有效（降级输出）</li>
 * </ul>
 */
@Data
@Builder
public class DocumentParseResult {

    // ─────────────────────────── 来源信息 ───────────────────────────
    /** 文件名（含扩展名），如 "合同模板_v2.docx" */
    private String fileName;

    /**
     * 文件来源标识。
     * 上传文件时为文件名；URL 来源时为完整 URL；数据库来源时为记录 ID。
     * 供下游溯源使用。
     */
    private String sourceId;

    /**
     * Tika 检测到的 MIME 类型，如 "application/pdf"。
     * 比文件扩展名更可靠，防止扩展名伪造。
     */
    private String mimeType;

    // ─────────────────────────── 内容 ───────────────────────────────
    /**
     * 提取的纯文本内容。
     * 已做基础清洗（去除多余空行、规范换行符）。
     * 若文件为图片/扫描 PDF 且未启用 OCR，此字段可能为空。
     */
    private String content;

    /** 提取到的字符数（content.length()），方便下游无需重新计算 */
    private int contentLength;

    /**
     * 页面/Sheet/Slide 数量。
     * PDF 为页数，PPT 为幻灯片数，Excel 为 Sheet 数，其他格式为 1。
     */
    private int pageCount;

    // ─────────────────────────── 元数据 ─────────────────────────────
    /**
     * 文档原始元数据，来自 Tika 解析结果。
     * 常见键值（不同格式支持情况不同）：
     * <ul>
     *   <li>dc:creator / Author    —— 作者</li>
     *   <li>dcterms:created        —— 创建时间（ISO 8601）</li>
     *   <li>dcterms:modified       —— 修改时间</li>
     *   <li>dc:title               —— 文档标题</li>
     *   <li>xmpTPg:NPages          —— 页数（PDF）</li>
     *   <li>Content-Length         —— 文件大小（字节）</li>
     * </ul>
     */
    private Map<String, String> metadata;

    // ─────────────────────────── 解析状态 ───────────────────────────
    /** 解析状态 */
    private ParseStatus status;

    /**
     * 解析过程中遇到的非致命错误或警告列表。
     * 非空时说明部分内容可能丢失，调用方应记录日志并人工复核。
     */
    private List<String> parseErrors;

    // ─────────────────────────── 时间戳 ─────────────────────────────
    /** 解析开始时间 */
    private Instant parseStartTime;
    /** 解析结束时间 */
    private Instant parseEndTime;
    /** 解析耗时（毫秒） */
    private long parseDurationMs;

    // ─────────────────────────── 枚举 ───────────────────────────────
    /**
     * 解析状态枚举
     */
    public enum ParseStatus {
        /** 解析成功，内容完整 */
        SUCCESS,
        /** 解析部分成功，content 有效但 parseErrors 非空 */
        PARTIAL,
        /** 解析失败，content 为 null 或空 */
        FAILED,
        /** 文件类型不支持 */
        UNSUPPORTED,
        /** 解析超时被中断 */
        TIMEOUT
    }
}