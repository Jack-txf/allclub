package com.feng.rag.datasource.parse;

import com.feng.rag.datasource.common.DocumentParseResult;
import com.feng.rag.datasource.config.DataSourceProperties;
import com.feng.rag.datasource.util.TextCleanupUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 Apache Tika 的通用文档解析器
 *
 * <p>这是整个解析体系的核心实现。Tika 的 AutoDetectParser 会根据 MIME 类型
 * 自动路由到对应的专用解析器：
 */
@Slf4j
@Component
public class TikaDocumentParser implements DocumentParser {

    // ─────────────────────────── 依赖注入 ───────────────────────────

    /** Tika 自动检测解析器，线程安全，单例复用 */
    private final AutoDetectParser autoDetectParser;

    /** PDF 解析配置（是否 OCR、是否提取注释等） */
    private final PDFParserConfig pdfParserConfig;

    /** OCR 配置 */
    private final TesseractOCRConfig tesseractOCRConfig;

    /** 模块配置 */
    private final DataSourceProperties properties;

    /** Micrometer 指标注册表，用于上报解析耗时、成功率等 */
    private final MeterRegistry meterRegistry;

    public TikaDocumentParser(AutoDetectParser autoDetectParser,
                               PDFParserConfig pdfParserConfig,
                               TesseractOCRConfig tesseractOCRConfig,
                               DataSourceProperties properties,
                               MeterRegistry meterRegistry) {
        this.autoDetectParser = autoDetectParser;
        this.pdfParserConfig = pdfParserConfig;
        this.tesseractOCRConfig = tesseractOCRConfig;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    // ─────────────────────────── 接口实现 ───────────────────────────

    @Override
    public List<String> supportedMimeTypes() {
        // 此实现是"兜底"解析器，支持所有 Tika 能处理的格式
        // 具体格式白名单由 DataSourceProperties.allowedMimeTypes 控制
        return List.of("*/*");
    }

    /**
     * 核心解析方法。
     */
    @Override
    public DocumentParseResult parse(InputStream inputStream, String fileName, String sourceId) {
        Instant startTime = Instant.now();
        List<String> parseErrors = new ArrayList<>();

        log.info("[TikaParser] 开始解析文档: fileName={}, sourceId={}", fileName, sourceId);

        // Micrometer Timer：记录解析耗时，自动上报 Prometheus
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // ── 步骤1：构建 ParseContext ──────────────────────────────
            ParseContext parseContext = buildParseContext();
            // ── 步骤2：准备 Metadata 和 ContentHandler ────────────────
            Metadata metadata = new Metadata();
            // 提前设置文件名，帮助 Tika 在 MIME 检测失败时作为辅助判断依据
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
            /*
             * BodyContentHandler 说明：
             * - 参数 maxContentLength 限制最大字符数，超出时抛出 SAXException（写满即停）
             * - 设为 -1 表示不限制（高风险，大文件会 OOM）
             * - 生产环境务必设置合理上限（默认 500 万字符 约等于 5MB 纯文本）
             */
            int maxLen = properties.getParser().getMaxContentLength();
            BodyContentHandler contentHandler = new BodyContentHandler(maxLen);

            // ── 步骤3：执行解析 ───────────────────────────────────────
            /*
             * AutoDetectParser.parse() 是阻塞调用，对于大 PDF 可能耗时数十秒。
             * 超时控制在上层 DocumentParserService 中通过 Future.get(timeout) 实现，
             * 此处无需重复处理超时。
             */
            // TikaInputStream tikaInputStream = TikaInputStream.get(inputStream);
            autoDetectParser.parse(inputStream, contentHandler, metadata, parseContext);

            // ── 步骤4：提取文本内容 ───────────────────────────────────
            String rawContent = contentHandler.toString();
            // ── 步骤5：文本清洗 ─────────────────────清洗第一层：确定性规则清洗──────
            String cleanedContent = TextCleanupUtils.clean(rawContent);
            // ── 步骤6：提取元数据 ─────────────────────────────────────
            Map<String, String> metadataMap = extractMetadata(metadata);
            // ── 步骤7：计算耗时并上报指标 ────────────────────────────
            Instant endTime = Instant.now();
            long durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();

            sample.stop(Timer.builder("rag.document.parse.duration")
                .tag("parser", parserName())
                .tag("status", "success")
                .description("文档解析耗时")
                .register(meterRegistry));

            meterRegistry.counter("rag.document.parse.count",
                "parser", parserName(), "status", "success").increment();

            log.info("[TikaParser] 解析成功: fileName={}, contentLength={}, durationMs={}",
                fileName, cleanedContent.length(), durationMs);

            // ── 步骤8：构建结果 ───────────────────────────────────────
            DocumentParseResult.ParseStatus status = DocumentParseResult.ParseStatus.SUCCESS;
            return DocumentParseResult.builder()
                .fileName(fileName)
                .sourceId(sourceId)
                .mimeType(metadata.get(Metadata.CONTENT_TYPE))
                .content(cleanedContent)
                .contentLength(cleanedContent.length())
                .pageCount(extractPageCount(metadata))
                .metadata(metadataMap)
                .status(status)
                .parseErrors(parseErrors)
                .parseStartTime(startTime)
                .parseEndTime(endTime)
                .parseDurationMs(durationMs)
                .build();

        } catch (SAXException e) {
            /*
             * SAXException 在两种情况下抛出：
             * 1. BodyContentHandler 达到 maxContentLength 上限（写满截断）
             * 2. 文档 XML 结构损坏
             *
             * 情况1 是预期行为，此时 contentHandler 已有部分内容，可降级返回。
             * 通过异常消息区分：Tika 写满时抛出特定消息。
             */
            if (e.getMessage() != null && e.getMessage().contains("limit")) {
                log.warn("[TikaParser] 文档内容超过最大长度限制，已截断: fileName={}", fileName);
                parseErrors.add("内容超过最大长度限制 " + properties.getParser().getMaxContentLength() + " 字符，已截断");
                // 降级：返回已截断的内容
                return buildPartialResult(fileName, sourceId, startTime, parseErrors, "（内容已截断）");
            }

            return handleParseFailure(fileName, sourceId, startTime, parseErrors,
                "文档 XML 结构解析失败", e, sample);

        } catch (TikaException e) {
            // Tika 内部解析错误（格式损坏、不支持的子格式等）
            return handleParseFailure(fileName, sourceId, startTime, parseErrors,
                "Tika 解析失败: " + e.getMessage(), e, sample);
        } catch (IOException e) {
            // IO 错误（流断开、磁盘问题等）
            return handleParseFailure(fileName, sourceId, startTime, parseErrors,
                "读取文档时发生 IO 错误", e, sample);
        } catch (Exception e) {
            // 兜底：未预期异常，绝对不向上抛出，保证解析方法永远有返回值
            return handleParseFailure(fileName, sourceId, startTime, parseErrors,
                "解析时发生未知错误: " + e.getClass().getSimpleName(), e, sample);
        }
    }

    // ─────────────────────────── 私有辅助方法 ───────────────────────
    /**
     * 构建 ParseContext，注入各格式专用配置。
     */
    private ParseContext buildParseContext() {
        ParseContext context = new ParseContext();
        // 注入 PDF 配置（控制 OCR 策略、书签提取等）
        context.set(PDFParserConfig.class, pdfParserConfig);
        // 注入 OCR 配置（仅当 OCR 启用时生效）
        if (properties.getParser().getOcr().isEnabled()) {
            context.set(TesseractOCRConfig.class, tesseractOCRConfig);
        }
        /*
         * 注意：此处不设置 Parser.class（递归解析器）。
         * 若设置为 autoDetectParser，Tika 会递归解析内嵌附件（如 PDF 内嵌 Word）。
         * 递归解析会大幅增加内存和时间消耗，生产环境默认关闭。
         * 如需开启：context.set(Parser.class, autoDetectParser);
         */
        return context;
    }

    /**
     * 从 Tika Metadata 中提取所有元数据到 Map。
     */
    private Map<String, String> extractMetadata(Metadata metadata) {
        if (!properties.getParser().isExtractMetadata()) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        for (String name : metadata.names()) {
            String[] values = metadata.getValues(name);
            if (values != null && values.length > 0) {
                // 多值字段用分号合并（如多位作者）
                result.put(name, String.join("; ", values));
            }
        }
        return result;
    }

    /**
     * 从元数据中提取页数。
     */
    private int extractPageCount(Metadata metadata) {
        // 按优先级尝试不同的键名
        String[] pageCountKeys = {
            "xmpTPg:NPages",
            "meta:page-count",
            "Page-Count",
            "Slide-Count"
        };

        for (String key : pageCountKeys) {
            String value = metadata.get(key);
            if (value != null && !value.isBlank()) {
                try {
                    return Integer.parseInt(value.trim());
                } catch (NumberFormatException ignored) {
                    // 继续尝试下一个键
                }
            }
        }
        return 1;  // 默认返回 1（无法获取时）
    }

    /**
     * 处理解析失败，构建 FAILED 状态的结果，并记录指标。
     */
    private DocumentParseResult handleParseFailure(
            String fileName, String sourceId, Instant startTime,
            List<String> parseErrors, String errorMessage, Exception e,
            Timer.Sample sample) {

        log.error("[TikaParser] 解析失败: fileName={}, error={}", fileName, errorMessage, e);

        parseErrors.add(errorMessage);

        sample.stop(Timer.builder("rag.document.parse.duration")
            .tag("parser", parserName())
            .tag("status", "failed")
            .register(meterRegistry));

        meterRegistry.counter("rag.document.parse.count",
            "parser", parserName(), "status", "failed").increment();

        Instant endTime = Instant.now();
        return DocumentParseResult.builder()
            .fileName(fileName)
            .sourceId(sourceId)
            .content("")
            .contentLength(0)
            .pageCount(0)
            .metadata(Map.of())
            .status(DocumentParseResult.ParseStatus.FAILED)
            .parseErrors(parseErrors)
            .parseStartTime(startTime)
            .parseEndTime(endTime)
            .parseDurationMs(endTime.toEpochMilli() - startTime.toEpochMilli())
            .build();
    }

    /**
     * 构建部分成功结果（内容截断场景）。
     */
    private DocumentParseResult buildPartialResult(
            String fileName, String sourceId, Instant startTime,
            List<String> parseErrors, String note) {

        Instant endTime = Instant.now();
        return DocumentParseResult.builder()
            .fileName(fileName)
            .sourceId(sourceId)
            .content(note)
            .contentLength(note.length())
            .pageCount(1)
            .metadata(Map.of())
            .status(DocumentParseResult.ParseStatus.PARTIAL)
            .parseErrors(parseErrors)
            .parseStartTime(startTime)
            .parseEndTime(endTime)
            .parseDurationMs(endTime.toEpochMilli() - startTime.toEpochMilli())
            .build();
    }
}