package com.feng.rag.datasource.service;

import com.feng.rag.datasource.common.DocumentParseResult;
import com.feng.rag.datasource.config.DataSourceProperties;
import com.feng.rag.datasource.exception.DocumentParseException;
import com.feng.rag.datasource.parse.TikaDocumentParser;
import com.feng.rag.datasource.util.MimeTypeDetector;
import com.feng.rag.datasource.util.TextCleanupUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 文档解析核心服务
 *
 * <p>职责：
 * <ul>
 *   <li>文件大小校验</li>
 *   <li>MIME 类型检测与白名单校验</li>
 *   <li>解析超时控制（通过 CompletableFuture + get(timeout) 实现）</li>
 *   <li>调用 TikaDocumentParser 执行实际解析</li>
 *   <li>批量解析支持</li>
 * </ul>
 *
 * <p>超时控制设计说明：
 * Tika 解析某些格式（超大 PDF、恶意构造的 Office 文件）可能长时间阻塞。
 * 通过将解析任务提交到专用线程池，主线程等待固定时间后强制取消，
 * 避免占用请求线程导致接口超时。
 */
@Slf4j
@Service
public class DocumentParserService {

    private final TikaDocumentParser tikaParser; // Tika 解析器
    private final MimeTypeDetector mimeTypeDetector; // MIME 类型检测器
    private final DataSourceProperties properties; // 配置项
    private final Executor documentParseExecutor; // 专用线程池

    public DocumentParserService(
            TikaDocumentParser tikaParser,
            MimeTypeDetector mimeTypeDetector,
            DataSourceProperties properties,
            @Qualifier("documentParseExecutor") Executor documentParseExecutor) {
        this.tikaParser = tikaParser;
        this.mimeTypeDetector = mimeTypeDetector;
        this.properties = properties;
        this.documentParseExecutor = documentParseExecutor;
    }

    // ─────────────────────────── 主入口 ─────────────────────────────
    /**
     * 解析文档字节数组（最常用的入口）。
     */
    public DocumentParseResult parseBytes(byte[] fileBytes, String fileName, String sourceId) {
        log.info("[ParserService] 开始处理文档: fileName={}, size={}KB, sourceId={}",
            fileName, fileBytes.length / 1024, sourceId);

        // ── 校验1：文件大小 ──────────────────────────────────────────
        validateFileSize(fileBytes.length, fileName);

        // ── 校验2：MIME 类型白名单 ────────────────────────────────────
        // 注意：MIME 检测需要读取流，ByteArrayInputStream 支持 reset
        ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
        String mimeType = mimeTypeDetector.detect(bais, fileName);
        validateMimeType(mimeType, fileName);
        // 检测后重置流位置，让 Tika 解析器从头读取
        bais.reset();
        log.debug("[ParserService] 文件校验通过: fileName={}, mimeType={}", fileName, mimeType);

        // ── 执行解析（含超时控制）────────────────────────────────────
        return parseWithTimeout(bais, fileName, sourceId);
    }

    /**
     * 解析 InputStream（适用于 URL 下载、数据库 BLOB 等场景）。
     */
    public DocumentParseResult parseStream(InputStream inputStream, String fileName, String sourceId) {
        try {
            // 读入内存，统一走 parseBytes 处理（含大小校验、MIME 检测）
            byte[] bytes = inputStream.readAllBytes();
            return parseBytes(bytes, fileName, sourceId);
        } catch (java.io.IOException e) {
            log.error("[ParserService] 读取 InputStream 失败: fileName={}", fileName, e);
            return buildFailedResult(fileName, sourceId, "读取输入流失败: " + e.getMessage());
        }
    }

    /**
     * 解析纯文本（rawText 直接提交场景，不需要 Tika 解析）。
     */
    public DocumentParseResult parseRawText(String rawText, String fileName, String sourceId) {
        Instant now = Instant.now();
        String cleaned = TextCleanupUtils.lightClean(rawText);

        return DocumentParseResult.builder()
            .fileName(fileName != null ? fileName : "raw_text.txt")
            .sourceId(sourceId)
            .mimeType("text/plain")
            .content(cleaned)
            .contentLength(cleaned.length())
            .pageCount(1)
            .metadata(Map.of("source", "raw_text"))
            .status(DocumentParseResult.ParseStatus.SUCCESS)
            .parseErrors(List.of())
            .parseStartTime(now)
            .parseEndTime(now)
            .parseDurationMs(0)
            .build();
    }

    /**
     * 批量解析文档（并行执行，汇总结果）。
     */
    public List<DocumentParseResult> parseBatch(
            List<ParseRequest> requests) {
        log.info("[ParserService] 批量解析开始，共 {} 个文档", requests.size());
        // 并行提交所有解析任务
        List<CompletableFuture<DocumentParseResult>> futures = requests.stream()
            .map(req -> CompletableFuture.supplyAsync(
                () -> parseBytes(req.fileBytes(), req.fileName(), req.sourceId()),
                documentParseExecutor
            ))
            .toList();

        // 等待全部完成（单个任务超时已在 parseWithTimeout 内部处理）
        return futures.stream()
            .map(f -> {
                try {
                    return f.join();  // 已在 parseWithTimeout 内处理超时，此处不会无限等待
                } catch (Exception e) {
                    log.error("[ParserService] 批量解析中某个任务异常", e);
                    return buildFailedResult("unknown", "batch", "批量解析异常: " + e.getMessage());
                }
            })
            .toList();
    }

    // ─────────────────────────── 超时控制 ───────────────────────────
    /**
     * 带超时控制的解析执行。
     */
    private DocumentParseResult parseWithTimeout(
            InputStream inputStream, String fileName, String sourceId) {

        int timeoutSeconds = properties.getParser().getParseTimeoutSeconds();

        CompletableFuture<DocumentParseResult> future = CompletableFuture.supplyAsync(
            () -> tikaParser.parse(inputStream, fileName, sourceId),
            documentParseExecutor
        );

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            future.cancel(true);
            log.error("[ParserService] 解析超时: fileName={}, timeoutSeconds={}", fileName, timeoutSeconds);

            return DocumentParseResult.builder()
                .fileName(fileName)
                .sourceId(sourceId)
                .content("")
                .contentLength(0)
                .pageCount(0)
                .metadata(Map.of())
                .status(DocumentParseResult.ParseStatus.TIMEOUT)
                .parseErrors(List.of(
                    String.format("解析超时（超过 %d 秒），文件可能过大或结构异常", timeoutSeconds)
                ))
                .parseStartTime(Instant.now())
                .parseEndTime(Instant.now())
                .parseDurationMs(timeoutSeconds * 1000L)
                .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[ParserService] 解析线程被中断: fileName={}", fileName, e);
            return buildFailedResult(fileName, sourceId, "解析线程被中断");

        } catch (java.util.concurrent.ExecutionException e) {
            log.error("[ParserService] 解析任务执行异常: fileName={}", fileName, e.getCause());
            return buildFailedResult(fileName, sourceId,
                "解析执行异常: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    // ─────────────────────────── 校验 ────────────────────────────────
    /**
     * 校验文件大小是否在允许范围内。
     */
    private void validateFileSize(long sizeBytes, String fileName) {
        long maxBytes = properties.getParser().getMaxFileSizeBytes();
        if (sizeBytes > maxBytes) {
            log.warn("[ParserService] 文件大小超限: fileName={}, size={}MB, max={}MB",
                fileName, sizeBytes / 1024 / 1024, maxBytes / 1024 / 1024);
            throw new DocumentParseException(
                "FILE_SIZE_EXCEEDED",
                String.format("文件 [%s] 大小 %dMB 超过最大限制 %dMB",
                    fileName, sizeBytes / 1024 / 1024, maxBytes / 1024 / 1024),
                413
            );
        }
    }

    /**
     * 校验 MIME 类型是否在白名单中。
     */
    private void validateMimeType(String mimeType, String fileName) {
        List<String> allowedTypes = properties.getAllowedMimeTypes();
        // 去除 MIME 类型中的参数部分（如 "text/plain; charset=UTF-8" → "text/plain"）
        String baseMimeType = mimeType != null && mimeType.contains(";")
            ? mimeType.substring(0, mimeType.indexOf(';')).trim()
            : mimeType;
        boolean allowed = allowedTypes.stream()
            .anyMatch(a -> a.equalsIgnoreCase(baseMimeType));

        if (!allowed) {
            log.warn("[ParserService] 文件类型不在白名单: fileName={}, mimeType={}", fileName, mimeType);
            throw new DocumentParseException(
                "UNSUPPORTED_FILE_TYPE",
                String.format("不支持的文件类型 [%s]，检测到 MIME 类型：%s", fileName, mimeType),
                415
            );
        }
    }

    // ─────────────────────────── 辅助方法 ───────────────────────────
    /** 构建失败状态的结果对象 */
    private DocumentParseResult buildFailedResult(String fileName, String sourceId, String errorMessage) {
        Instant now = Instant.now();
        return DocumentParseResult.builder()
            .fileName(fileName)
            .sourceId(sourceId)
            .content("")
            .contentLength(0)
            .pageCount(0)
            .metadata(Map.of())
            .status(DocumentParseResult.ParseStatus.FAILED)
            .parseErrors(List.of(errorMessage))
            .parseStartTime(now)
            .parseEndTime(now)
            .parseDurationMs(0)
            .build();
    }

    // ─────────────────────────── 内部 Record ────────────────────────
    /**
     * 批量解析请求条目（Java 21 Record，不可变值对象）
     */
    public record ParseRequest(byte[] fileBytes, String fileName, String sourceId) {}
}