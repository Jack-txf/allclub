package com.feng.rag.controller;

import com.feng.rag.chunk.model.Chunk;
import com.feng.rag.chunk.model.ChunkingOptions;
import com.feng.rag.chunk.model.ChunkingResult;
import com.feng.rag.chunk.service.ChunkingService;
import com.feng.rag.chunk.strategy.ChunkingStrategy;
import com.feng.rag.datasource.common.DataSourceRequest;
import com.feng.rag.datasource.common.DocumentParseResult;
import com.feng.rag.datasource.service.DataSourceIngestionService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分块模块 REST 接口
 *
 * <p>提供分块相关的 HTTP API</p>
 */
@Slf4j
@RestController
@RequestMapping("/v1/chunk")
@RequiredArgsConstructor
public class ChunkController {

    private final ChunkingService chunkingService;
    private final DataSourceIngestionService ingestionService;

    /**
     * 上传一个文件，并对文件进行分块 -- 测试方法
     */
    @PostMapping("/file")
    @Timed(value = "rag.api.chunk.file", description = "文件上传分块接口耗时")
    public R chunkFile(@RequestPart("file") MultipartFile file,
                       @RequestParam(value = "strategy", required = false) String strategy,
                       @RequestParam(value = "sourceId", required = false) String sourceId) {
        // 1. 文件解析
        DataSourceRequest request = new DataSourceRequest();
        request.setSourceType(DataSourceRequest.SourceType.FILE_UPLOAD);
        request.setFile(file);
        request.setSourceId(sourceId);
        DocumentParseResult result = ingestionService.ingest(request);
        // 2.测试分块
        ChunkingResult result1 = chunkingService.chunk(result, strategy);
        return R.ok().add("result", result1);
    }


    /**
     * 对文本进行分块
     */
    @PostMapping("/text")
    @Timed(value = "rag.api.chunk.text", description = "文本分块接口耗时")
    public R chunkText(@RequestBody @Valid ChunkTextRequest request) {
        log.info("[ChunkController] 文本分块请求: strategy={}, textLength={}",
                request.strategy(), request.text().length());

        ChunkingOptions options = convertToOptions(request.options());

        ChunkingResult result = chunkingService.chunkText(
                request.text(),
                request.documentId() != null ? request.documentId() : "text_" + System.currentTimeMillis(),
                request.documentName() != null ? request.documentName() : "unnamed.txt",
                request.strategy(),
                options
        );

        return wrapResult(result);
    }

    /**
     * 使用默认策略对文本分块
     */
    @PostMapping("/text/default")
    @Timed(value = "rag.api.chunk.text.default", description = "默认策略文本分块接口耗时")
    public R chunkTextDefault(@RequestBody @Valid ChunkTextSimpleRequest request) {
        log.info("[ChunkController] 默认策略文本分块: textLength={}", request.text().length());

        ChunkingResult result = chunkingService.chunkText(
                request.text(),
                request.documentId() != null ? request.documentId() : "text_" + System.currentTimeMillis(),
                request.documentName() != null ? request.documentName() : "unnamed.txt",
                null,
                null
        );

        return wrapResult(result);
    }

    /**
     * 对文档解析结果进行分块
     */
    @PostMapping("/document")
    @Timed(value = "rag.api.chunk.document", description = "文档分块接口耗时")
    public R chunkDocument(@RequestBody @Valid ChunkDocumentRequest request) {
        log.info("[ChunkController] 文档分块请求: document={}, strategy={}",
                request.document().getFileName(), request.strategy());

        ChunkingOptions options = convertToOptions(request.options());
        ChunkingResult result = chunkingService.chunk(request.document(), request.strategy(), options);

        return wrapResult(result);
    }

    /**
     * 获取所有可用的分块策略
     */
    @GetMapping("/strategies")
    public R getStrategies() {
        Map<String, Object> strategies = chunkingService.getAllStrategies().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Map.of(
                                "name", e.getValue().getStrategyName(),
                                "description", e.getValue().getStrategyDescription()
                        )
                ));

        return R.ok().add("strategies", strategies);
    }

    /**
     * 获取分块策略详情
     */
    @GetMapping("/strategies/{name}")
    public R getStrategyDetail(@PathVariable String name) {
        ChunkingStrategy strategy = chunkingService.getStrategy(name);
        if (strategy == null) {
            return R.error(404, "策略不存在: " + name);
        }

        return R.ok()
                .add("name", strategy.getStrategyName())
                .add("description", strategy.getStrategyDescription())
                .add("defaultOptions", strategy.getDefaultOptions());
    }

    // ==================== 私有方法 ====================

    private ChunkingOptions convertToOptions(ChunkOptionsRequest request) {
        if (request == null) {
            return null;
        }

        ChunkingOptions.ChunkingOptionsBuilder builder = ChunkingOptions.builder();

        if (request.targetChunkSize() != null) {
            builder.targetChunkSize(request.targetChunkSize());
        }
        if (request.minChunkSize() != null) {
            builder.minChunkSize(request.minChunkSize());
        }
        if (request.maxChunkSize() != null) {
            builder.maxChunkSize(request.maxChunkSize());
        }
        if (request.overlapSize() != null) {
            builder.overlapSize(request.overlapSize());
        }
        if (request.respectParagraphBoundaries() != null) {
            builder.respectParagraphBoundaries(request.respectParagraphBoundaries());
        }
        if (request.respectSentenceBoundaries() != null) {
            builder.respectSentenceBoundaries(request.respectSentenceBoundaries());
        }

        return builder.build();
    }

    private R wrapResult(ChunkingResult result) {
        if (result.getStatus() == ChunkingResult.Status.FAILED) {
            return R.error(result.getErrorMessage());
        }

        return R.ok()
                .add("documentId", result.getDocumentId())
                .add("documentName", result.getDocumentName())
                .add("strategy", result.getStrategyName())
                .add("totalChunks", result.getTotalChunks())
                .add("validChunks", result.getValidChunks())
                .add("averageChunkSize", result.getAverageChunkSize())
                .add("averageQualityScore", result.getAverageQualityScore())
                .add("durationMs", result.getDurationMs())
                .add("sizeDistribution", result.getSizeDistribution())
                .add("chunks", result.getChunks().stream()
                        .map(this::convertChunkToMap)
                        .toList());
    }

    private Map<String, Object> convertChunkToMap(Chunk chunk) {
        return Map.of(
                "chunkId", chunk.getChunkId(),
                "chunkIndex", chunk.getChunkIndex(),
                "content", chunk.getContent(),
                "contentLength", chunk.getContentLength(),
                "startOffset", chunk.getStartOffset(),
                "endOffset", chunk.getEndOffset(),
                "chunkType", chunk.getChunkType(),
                "qualityScore", chunk.getQualityScore(),
                "startsWithCompleteSentence", chunk.isStartsWithCompleteSentence(),
                "endsWithCompleteSentence", chunk.isEndsWithCompleteSentence()
        );
    }

    // ==================== 请求记录 ====================

    public record ChunkTextRequest(
            @NotBlank(message = "文本内容不能为空") String text,
            String documentId,
            String documentName,
            String strategy,
            ChunkOptionsRequest options
    ) {}

    public record ChunkTextSimpleRequest(
            @NotBlank(message = "文本内容不能为空") String text,
            String documentId,
            String documentName
    ) {}

    public record ChunkDocumentRequest(
            @NotNull(message = "文档不能为空") DocumentParseResult document,
            String strategy,
            ChunkOptionsRequest options
    ) {}

    public record ChunkOptionsRequest(
            Integer targetChunkSize,
            Integer minChunkSize,
            Integer maxChunkSize,
            Integer overlapSize,
            Boolean respectParagraphBoundaries,
            Boolean respectSentenceBoundaries
    ) {}
}
