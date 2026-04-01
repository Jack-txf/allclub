package com.feng.rag.controller;


import com.feng.rag.datasource.common.DataSourceRequest;
import com.feng.rag.datasource.common.DocumentParseResult;
import com.feng.rag.datasource.exception.DocumentParseException;
import com.feng.rag.datasource.service.DataSourceIngestionService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/datasource")
public class DataSourceController {

    private final DataSourceIngestionService ingestionService;
    public DataSourceController(DataSourceIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    // ─────────────────────────── 接口1：文件上传 【最重要的】────────────────────
    /**
     * 文件上传解析接口。
     *
     * <p>支持 multipart/form-data 方式上传文件，格式包含：
     * PDF、Word（doc/docx）、PPT（ppt/pptx）、Excel（xls/xlsx）、TXT、HTML 等。
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Timed(value = "rag.api.upload", description = "文件上传解析接口耗时")
    public ResponseEntity<ApiResponse<DocumentParseResult>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "sourceId", required = false) String sourceId) {

        log.info("[Controller] 文件上传接口调用: fileName={}, sourceId={}",
                file.getOriginalFilename(), sourceId);

        DataSourceRequest request = new DataSourceRequest();
        request.setSourceType(DataSourceRequest.SourceType.FILE_UPLOAD);
        request.setFile(file);
        request.setSourceId(sourceId);

        DocumentParseResult result = ingestionService.ingest(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ─────────────────────────── 接口2：URL 解析 ────────────────────
    @PostMapping("/url")
    @Timed(value = "rag.api.url", description = "URL 文档解析接口耗时")
    public ResponseEntity<ApiResponse<DocumentParseResult>> parseUrl(
            @RequestBody @Valid UrlParseRequest urlRequest) {

        log.info("[Controller] URL 解析接口调用: url={}", urlRequest.url());

        DataSourceRequest request = new DataSourceRequest();
        request.setSourceType(DataSourceRequest.SourceType.URL);
        request.setUrl(urlRequest.url());
        request.setSourceId(urlRequest.sourceId());

        DocumentParseResult result = ingestionService.ingest(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ─────────────────────────── 接口3：原始文本 ─────────────────────
    @PostMapping("/text")
    @Timed(value = "rag.api.text", description = "原始文本接入接口耗时")
    public ResponseEntity<ApiResponse<DocumentParseResult>> parseText(
            @RequestBody @Valid TextParseRequest textRequest) {

        log.info("[Controller] 原始文本接入: length={}", textRequest.text().length());

        DataSourceRequest request = new DataSourceRequest();
        request.setSourceType(DataSourceRequest.SourceType.RAW_TEXT);
        request.setRawText(textRequest.text());
        request.setRawTextFileName(textRequest.fileName());
        request.setSourceId(textRequest.sourceId());

        DocumentParseResult result = ingestionService.ingest(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ─────────────────────────── 接口4：批量 URL ─────────────────────
    @PostMapping("/batch")
    @Timed(value = "rag.api.batch", description = "批量解析接口耗时")
    public ResponseEntity<ApiResponse<List<DocumentParseResult>>> parseBatch(
            @RequestBody @Valid BatchParseRequest batchRequest) {

        if (batchRequest.urls().size() > 20) {
            throw new DocumentParseException("BATCH_TOO_LARGE",
                    "单次批量请求最多 20 个 URL，当前：" + batchRequest.urls().size(), 400);
        }

        log.info("[Controller] 批量解析接口调用: urlCount={}", batchRequest.urls().size());

        List<DataSourceRequest> requests = batchRequest.urls().stream()
                .map(url -> {
                    DataSourceRequest req = new DataSourceRequest();
                    req.setSourceType(DataSourceRequest.SourceType.URL);
                    req.setUrl(url);
                    req.setSourceId(url);
                    return req;
                })
                .toList();

        List<DocumentParseResult> results = ingestionService.ingestBatch(requests);
        return ResponseEntity.ok(ApiResponse.success(results));
    }


    // ─────────────────────────── Request Records ─────────────────────
    /** URL 解析请求体（Java 21 Record） */
    public record UrlParseRequest(
            @NotBlank(message = "url 不能为空") String url,
            String sourceId
    ) {}
    /** 文本接入请求体 */
    public record TextParseRequest(
            @NotBlank(message = "text 不能为空") String text,
            String fileName,
            String sourceId
    ) {}
    /** 批量解析请求体 */
    public record BatchParseRequest(
            @jakarta.validation.constraints.NotEmpty(message = "urls 列表不能为空")
            List<String> urls
    ) {}

    // ─────────────────────────── 统一响应包装 ────────────────────────
    /**
     * 统一 API 响应格式
     */
    public record ApiResponse<T>(
            boolean success,
            String message,
            T data,
            long timestamp
    ) {
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, "success", data, System.currentTimeMillis());
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null, System.currentTimeMillis());
        }
    }
}
