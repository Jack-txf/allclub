package com.feng.rag.datasource.service;

import com.feng.rag.datasource.common.DataSourceRequest;
import com.feng.rag.datasource.common.DocumentParseResult;
import com.feng.rag.datasource.exception.DocumentParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * 统一数据源接入服务
 */
@Slf4j
@Service
public class DataSourceIngestionService {

    private final DocumentParserService parserService;

    /**
     * Java 21 内置 HttpClient（JEP 321，正式 API）。
     * 单例复用，内部维护连接池，线程安全。
     * 使用 Virtual Thread executor（JEP 425），充分利用 Java 21 特性。
     */
    private final HttpClient httpClient;

    public DataSourceIngestionService(DocumentParserService parserService) {
        this.parserService = parserService;
        this.httpClient = HttpClient.newBuilder()
            // Java 21 Virtual Thread：每个请求分配一个虚拟线程，无需手动管理线程池
            .executor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor())
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    /**
     * 统一数据源接入主入口。
     */
    public DocumentParseResult ingest(DataSourceRequest request) {
        log.info("[IngestionService] 接收数据源接入请求: sourceType={}, sourceId={}",
            request.getSourceType(), request.getSourceId());

        return switch (request.getSourceType()) {
            // Java 21 switch expression：更简洁，编译器强制覆盖所有 case
            case FILE_UPLOAD -> ingestFile(request.getFile(), request.getSourceId());
            case URL         -> ingestUrl(request.getUrl(), request.getSourceId());
            case RAW_TEXT    -> ingestRawText(request.getRawText(),
                                    request.getRawTextFileName(), request.getSourceId());
        };
    }

    /**
     * 批量数据源接入（并行处理）。
     */
    public List<DocumentParseResult> ingestBatch(List<DataSourceRequest> requests) {
        log.info("[IngestionService] 批量接入，共 {} 个请求", requests.size());
        // 并行接入（Java 21 Stream + Virtual Thread 无缝配合）
        return requests.parallelStream()
            .map(this::ingestSafely)
            .toList();
    }

    // ─────────────────────────── 三种接入方式 ───────────────────────
    /**
     * 文件上传接入。
     */
    private DocumentParseResult ingestFile(MultipartFile file, String sourceId) {
        if (file == null || file.isEmpty()) {
            throw new DocumentParseException("EMPTY_FILE", "上传的文件为空", 400);
        }

        String fileName = file.getOriginalFilename() != null
            ? file.getOriginalFilename()
            : "unknown_" + System.currentTimeMillis();

        log.info("[IngestionService] 处理上传文件: fileName={}, size={}KB",
            fileName, file.getSize() / 1024);

        try {
            byte[] bytes = file.getBytes();
            return parserService.parseBytes(bytes, fileName, sourceId);
        } catch (IOException e) {
            log.error("[IngestionService] 读取上传文件失败: fileName={}", fileName, e);
            throw new DocumentParseException("FILE_READ_ERROR",
                "读取上传文件失败: " + e.getMessage(), 500, e);
        }
    }

    /**
     * URL 下载接入。
     */
    private DocumentParseResult ingestUrl(String url, String sourceId) {
        if (url == null || url.isBlank()) {
            throw new DocumentParseException("INVALID_URL", "URL 不能为空", 400);
        }
        // 基础 URL 格式校验（防止 SSRF：仅允许 http/https）
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new DocumentParseException("INVALID_URL",
                "URL 必须以 http:// 或 https:// 开头，不支持其他协议（安全限制）", 400);
        }

        log.info("[IngestionService] 开始下载 URL: {}", url);
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))  // 下载超时 30 秒
                .GET()
                // 设置 User-Agent，部分网站拒绝无 UA 的请求
                .header("User-Agent", "RAG-DataSource-Bot/1.0")
                .build();

            // 以字节数组接收响应（适合文档类，非流式大文件场景）
            HttpResponse<byte[]> response = httpClient.send(
                httpRequest, HttpResponse.BodyHandlers.ofByteArray()
            );
            if (response.statusCode() != 200) {
                throw new DocumentParseException("URL_FETCH_FAILED",
                    String.format("下载 URL [%s] 失败，HTTP 状态码：%d", url, response.statusCode()),
                    502);
            }

            // 从 URL 末尾推断文件名（如 https://example.com/doc/report.pdf → report.pdf）
            String fileName = extractFileNameFromUrl(url);
            byte[] bytes = response.body();
            log.info("[IngestionService] URL 下载完成: url={}, size={}KB", url, bytes.length / 1024);
            return parserService.parseBytes(bytes, fileName, sourceId != null ? sourceId : url);
        } catch (DocumentParseException e) {
            throw e;  // 已是业务异常，直接抛出
        } catch (IOException e) {
            log.error("[IngestionService] URL 下载 IO 异常: url={}", url, e);
            throw new DocumentParseException("URL_FETCH_FAILED",
                "下载文档失败（网络异常）: " + e.getMessage(), 502, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DocumentParseException("URL_FETCH_INTERRUPTED",
                "下载任务被中断", 500, e);
        }
    }

    /**
     * 原始文本接入（直接提交文本，无需解析）。
     */
    private DocumentParseResult ingestRawText(String rawText, String fileName, String sourceId) {
        if (rawText == null || rawText.isBlank()) {
            throw new DocumentParseException("EMPTY_TEXT", "原始文本内容不能为空", 400);
        }
        log.info("[IngestionService] 处理原始文本: length={}", rawText.length());
        return parserService.parseRawText(rawText, fileName, sourceId);
    }

    // ─────────────────────────── 工具方法 ───────────────────────────
    /**
     * 从 URL 中提取文件名。
     */
    private String extractFileNameFromUrl(String url) {
        try {
            // 去除查询参数
            String path = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
            // 取最后一段路径
            String lastSegment = path.substring(path.lastIndexOf('/') + 1);
            return lastSegment.isBlank() ? "downloaded_doc" : lastSegment;
        } catch (Exception e) {
            return "downloaded_doc";
        }
    }

    /**
     * 安全版接入：捕获所有异常，返回 FAILED 结果（用于批量场景）。
     */
    private DocumentParseResult ingestSafely(DataSourceRequest request) {
        try {
            return ingest(request);
        } catch (DocumentParseException e) {
            log.error("[IngestionService] 单条接入失败（批量模式）: sourceId={}, error={}",
                request.getSourceId(), e.getMessage());
            return DocumentParseResult.builder()
                .fileName("unknown")
                .sourceId(request.getSourceId())
                .content("")
                .contentLength(0)
                .pageCount(0)
                .metadata(java.util.Map.of())
                .status(DocumentParseResult.ParseStatus.FAILED)
                .parseErrors(List.of(e.getErrorCode() + ": " + e.getMessage()))
                .parseStartTime(java.time.Instant.now())
                .parseEndTime(java.time.Instant.now())
                .parseDurationMs(0)
                .build();
        }
    }
}