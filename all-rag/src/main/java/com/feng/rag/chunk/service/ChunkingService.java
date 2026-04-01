package com.feng.rag.chunk.service;

import com.feng.rag.chunk.config.ChunkingProperties;
import com.feng.rag.chunk.model.ChunkingOptions;
import com.feng.rag.chunk.model.ChunkingResult;
import com.feng.rag.chunk.strategy.ChunkingStrategy;
import com.feng.rag.datasource.common.DocumentParseResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分块服务 —— 企业级分块处理中心
 *
 * <p>职责：
 * <ul>
 *   <li>策略路由：根据文档类型和配置选择最佳分块策略</li>
 *   <li>配置管理：整合配置文件和运行时选项</li>
 *   <li>管道支持：支持多策略组合（如先递归后滑动窗口）</li>
 *   <li>指标监控：分块数量、大小分布、耗时等</li>
 *   <li>批量处理：高效处理大量文档</li>
 * </ul>
 */
@Slf4j
@Service
public class ChunkingService {

    private final Map<String, ChunkingStrategy> strategies;
    private final ChunkingProperties properties;
    private final MeterRegistry meterRegistry;

    public ChunkingService(List<ChunkingStrategy> strategyList,
                           @Qualifier("chunkingProperties") ChunkingProperties properties,
                          MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;

        // 注册所有策略
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(
                ChunkingStrategy::getStrategyName,
                s -> s,
                (s1, s2) -> s1  // 处理可能的重复
            ));

        log.info("[ChunkingService] 已注册 {} 种分块策略: {}",
            strategies.size(), strategies.keySet());
    }

    // ==================== 核心分块方法 ====================

    /**
     * 对文档进行分块（使用默认配置）
     */
    public ChunkingResult chunk(DocumentParseResult document) {
        return chunk(document, null, null);
    }

    /**
     * 对文档进行分块（使用指定策略）
     */
    public ChunkingResult chunk(DocumentParseResult document, String strategyName) {
        return chunk(document, strategyName, null);
    }

    /**
     * 对文档进行分块（使用指定策略和选项）
     */
    public ChunkingResult chunk(DocumentParseResult document, String strategyName,
                                 ChunkingOptions options) {
        Instant startTime = Instant.now();
        Timer.Sample sample = Timer.start(meterRegistry);

        // 1. 确定策略
        ChunkingStrategy strategy = selectStrategy(document, strategyName);
        if (strategy == null) {
            log.error("[ChunkingService] 未找到合适的分块策略: {}", strategyName);
            return ChunkingResult.failed(
                document != null ? document.getSourceId() : null,
                document != null ? document.getFileName() : null,
                "未找到合适的分块策略: " + strategyName
            );
        }
        // 2. 构建选项（合并配置和传入选项）
        ChunkingOptions finalOptions = buildOptions(document, options);
        // 3. 执行分块
        ChunkingResult result = strategy.chunk(document, finalOptions);
        // 4. 记录指标
        sample.stop(Timer.builder("rag.chunk.duration")
            .tag("strategy", strategy.getStrategyName())
            .tag("status", result.getStatus().name())
            .register(meterRegistry));
        meterRegistry.counter("rag.chunk.count",
            "strategy", strategy.getStrategyName(),
            "status", result.getStatus().name()).increment();

        if (result.getStatus() == ChunkingResult.Status.SUCCESS) {
            meterRegistry.gauge("rag.chunk.total_chunks",
                result.getTotalChunks());
            meterRegistry.gauge("rag.chunk.avg_size",
                result.getAverageChunkSize());
        }

        log.info("[ChunkingService] 分块完成: document={}, strategy={}, chunks={}, durationMs={}",
            document != null ? document.getFileName() : "unknown",
            strategy.getStrategyName(),
            result.getTotalChunks(),
            result.getDurationMs());

        return result;
    }

    /**
     * 对纯文本进行分块
     */
    public ChunkingResult chunkText(String text, String documentId, String documentName,
                                     String strategyName, ChunkingOptions options) {
        DocumentParseResult doc = DocumentParseResult.builder()
            .sourceId(documentId)
            .fileName(documentName)
            .content(text)
            .contentLength(text.length())
            .status(DocumentParseResult.ParseStatus.SUCCESS)
            .build();
        return chunk(doc, strategyName, options);
    }

    /**
     * 批量分块
     */
    public List<ChunkingResult> chunkBatch(List<DocumentParseResult> documents) {
        return chunkBatch(documents, null, null);
    }

    /**
     * 批量分块（使用指定策略）
     */
    public List<ChunkingResult> chunkBatch(List<DocumentParseResult> documents,
                                           String strategyName) {
        return chunkBatch(documents, strategyName, null);
    }

    /**
     * 批量分块（使用指定策略和选项）
     */
    public List<ChunkingResult> chunkBatch(List<DocumentParseResult> documents,
                                           String strategyName,
                                           ChunkingOptions options) {
        log.info("[ChunkingService] 开始批量分块: {} 个文档", documents.size());

        return documents.stream()
            .map(doc -> {
                try {
                    return chunk(doc, strategyName, options);
                } catch (Exception e) {
                    log.error("[ChunkingService] 分块失败: document={}", doc.getFileName(), e);
                    return ChunkingResult.failed(doc.getSourceId(), doc.getFileName(), e.getMessage());
                }
            })
            .toList();
    }

    // ==================== 策略管道 ====================

    /**
     * 策略管道：按顺序应用多个策略
     * 例如：先递归分块，再对结果进行滑动窗口增强
     */
    public ChunkingResult chunkWithPipeline(DocumentParseResult document,
                                             List<String> strategyPipeline,
                                             ChunkingOptions options) {
        if (strategyPipeline == null || strategyPipeline.isEmpty()) {
            return chunk(document, null, options);
        }

        log.info("[ChunkingService] 执行分块管道: {} 个策略", strategyPipeline.size());

        // 第一个策略处理原始文档
        ChunkingResult result = chunk(document, strategyPipeline.get(0), options);

        // 后续策略处理前一步的分块
        for (int i = 1; i < strategyPipeline.size(); i++) {
            String strategyName = strategyPipeline.get(i);
            ChunkingStrategy strategy = strategies.get(strategyName);

            if (strategy == null) {
                log.warn("[ChunkingService] 管道中的策略不存在: {}", strategyName);
                continue;
            }

            // 重新分块
            result = strategy.rechunk(result.getChunks(), options);
        }

        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 选择分块策略
     */
    private ChunkingStrategy selectStrategy(DocumentParseResult document, String preferredStrategy) {
        // 1. 优先使用指定策略
        if (preferredStrategy != null && !preferredStrategy.isEmpty()) {
            ChunkingStrategy strategy = strategies.get(preferredStrategy);
            if (strategy != null) {
                return strategy;
            }
            log.warn("[ChunkingService] 指定的策略不存在: {}，使用默认策略", preferredStrategy);
        }

        // 2. 从配置获取策略
        String configStrategy = properties.getDefaultStrategy();
        if (configStrategy != null && strategies.containsKey(configStrategy)) {
            return strategies.get(configStrategy);
        }

        // 3. 自动选择（根据文档特征）
        if (document != null) {
            return autoSelectStrategy(document);
        }

        // 4. 使用第一个可用策略
        return strategies.values().stream().findFirst().orElse(null);
    }

    /**
     * 自动选择策略
     */
    private ChunkingStrategy autoSelectStrategy(DocumentParseResult document) {
        String content = document.getContent();
        String mimeType = document.getMimeType();

        // 根据 MIME 类型选择
        if (mimeType != null) {
            if (mimeType.contains("code") || mimeType.contains("json") || mimeType.contains("xml")) {
                return strategies.get("recursive");  // 代码用递归策略
            }
        }

        // 根据内容特征选择
        if (content != null) {
            // 检查是否有大量标题
            long headingCount = content.lines()
                .filter(line -> line.matches("^#{1,6}\\s+"))
                .count();

            if (headingCount > 5) {
                return strategies.get("recursive");  // 结构清晰用递归
            }

            // 检查段落数量
            long paragraphCount = content.split("\n\n").length;
            if (paragraphCount > 20) {
                return strategies.get("semantic");  // 长文本用语义
            }

            // 内容较短用段落策略
            if (content.length() < 5000) {
                return strategies.get("paragraph");
            }
        }

        // 默认使用递归策略
        return strategies.getOrDefault("recursive",
            strategies.values().stream().findFirst().orElse(null));
    }

    /**
     * 构建分块选项
     */
    private ChunkingOptions buildOptions(DocumentParseResult document, ChunkingOptions userOptions) {
        ChunkingOptions.ChunkingOptionsBuilder builder = ChunkingOptions.builder();

        // 1. 从配置文件获取类型特定配置
        ChunkingProperties.ChunkConfig config = properties.getConfigForType(
            document != null ? document.getMimeType() : null,
            document != null ? getFileExtension(document.getFileName()) : null
        );

        // 2. 应用配置值
        if (config.getTargetChunkSize() != null) {
            builder.targetChunkSize(config.getTargetChunkSize());
        }
        if (config.getMinChunkSize() != null) {
            builder.minChunkSize(config.getMinChunkSize());
        }
        if (config.getMaxChunkSize() != null) {
            builder.maxChunkSize(config.getMaxChunkSize());
        }
        if (config.getOverlapSize() != null) {
            builder.overlapSize(config.getOverlapSize());
        }
        if (config.getRespectParagraphBoundaries() != null) {
            builder.respectParagraphBoundaries(config.getRespectParagraphBoundaries());
        }
        if (config.getRespectSentenceBoundaries() != null) {
            builder.respectSentenceBoundaries(config.getRespectSentenceBoundaries());
        }
        if (config.getEnableQualityFilter() != null) {
            builder.enableQualityFilter(config.getEnableQualityFilter());
        }
        if (config.getMinQualityScore() != null) {
            builder.minQualityScore(config.getMinQualityScore());
        }
        if (config.getExtractKeywords() != null) {
            builder.extractKeywords(config.getExtractKeywords());
        }

        // 3. 用户选项覆盖配置值
        if (userOptions != null) {
            // 使用反射或逐个字段覆盖
            overrideOptions(builder, userOptions);
        }

        return builder.build();
    }

    private void overrideOptions(ChunkingOptions.ChunkingOptionsBuilder builder, ChunkingOptions userOptions) {
        // 简化处理：如果用户提供了有效值，则覆盖
        if (userOptions.getTargetChunkSize() > 0) {
            builder.targetChunkSize(userOptions.getTargetChunkSize());
        }
        if (userOptions.getMinChunkSize() > 0) {
            builder.minChunkSize(userOptions.getMinChunkSize());
        }
        if (userOptions.getMaxChunkSize() > 0) {
            builder.maxChunkSize(userOptions.getMaxChunkSize());
        }
        if (userOptions.getOverlapSize() >= 0) {
            builder.overlapSize(userOptions.getOverlapSize());
        }
        // ... 其他字段
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return null;
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : null;
    }

    // ==================== 查询方法 ====================

    /**
     * 获取所有可用的分块策略
     */
    public Map<String, ChunkingStrategy> getAllStrategies() {
        return Collections.unmodifiableMap(strategies);
    }

    /**
     * 获取指定策略
     */
    public ChunkingStrategy getStrategy(String name) {
        return strategies.get(name);
    }

    /**
     * 检查策略是否存在
     */
    public boolean hasStrategy(String name) {
        return strategies.containsKey(name);
    }
}
