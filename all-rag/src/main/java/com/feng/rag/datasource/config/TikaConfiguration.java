package com.feng.rag.datasource.config;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Apache Tika 及解析线程池配置
 *
 * <p>Tika 的核心对象（AutoDetectParser、TikaConfig）是线程安全且创建代价较高的，
 * 应作为 Spring Bean 单例管理，避免每次解析重复初始化。
 */
@Configuration
public class TikaConfiguration {

    private final DataSourceProperties properties;
    public TikaConfiguration(DataSourceProperties properties) {
        this.properties = properties;
    }

    /**
     * Tika 门面对象。
     *
     * <p>提供简洁的 API（detect / parseToString），适合快速 MIME 类型检测场景。
     * 内部使用默认配置的 AutoDetectParser。
     */
    @Bean
    public Tika tika() {
        return new Tika();
    }

    /**
     * Tika 全局配置。
     *
     * <p>TikaConfig 从 classpath 的 tika-config.xml 或默认配置加载，
     * 包含所有已知格式的解析器映射。
     */
    @Bean
    public TikaConfig tikaConfig() {
        return TikaConfig.getDefaultConfig();
    }

    /**
     * 自动检测解析器。
     */
    @Bean
    public AutoDetectParser autoDetectParser(TikaConfig tikaConfig) {
        return new AutoDetectParser(tikaConfig);
    }

    /**
     * PDF 解析配置。
     */
    @Bean
    public PDFParserConfig pdfParserConfig() {
        PDFParserConfig config = new PDFParserConfig();
        // 策略：优先使用 PDF 内嵌文字；若为扫描件且已启用 OCR，则触发 OCR
        if (properties.getParser().getOcr().isEnabled()) {
            // NO_OCR：不做 OCR
            // OCR_ONLY：只做 OCR（全部走图像识别）
            // OCR_AND_TEXT_EXTRACTION：混合模式（推荐）
            config.setOcrStrategy(PDFParserConfig.OCR_STRATEGY.OCR_AND_TEXT_EXTRACTION);
        } else {
            config.setOcrStrategy(PDFParserConfig.OCR_STRATEGY.NO_OCR);
        }
        // 是否提取 PDF 注释（批注、高亮等），提取后追加到正文末尾
        config.setExtractAnnotationText(true);
        // 是否提取内嵌附件（PDF 可以内嵌其他 PDF）
        config.setExtractInlineImages(false);  // 图片内容不做提取，避免内存膨胀
        // 按页码顺序输出文字（默认按字符坐标排序，某些 PDF 顺序混乱）
        config.setSortByPosition(true);

        return config;
    }

    /**
     * Tesseract OCR 配置（仅当 ocr.enabled=true 时有意义）。
     */
    @Bean
    public TesseractOCRConfig tesseractOCRConfig() {
        TesseractOCRConfig config = new TesseractOCRConfig();
        config.setLanguage(properties.getParser().getOcr().getLanguage());
        // OCR 超时时间（秒），防止卡死
        config.setTimeoutSeconds(120);
        // DPI 设置，扫描件通常为 300dpi
        config.setOutputType(TesseractOCRConfig.OUTPUT_TYPE.TXT);
        return config;
    }

    /**
     * 文档解析专用线程池。
     */
    @Bean(name = "documentParseExecutor")
    public Executor documentParseExecutor() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cpuCores);
        executor.setMaxPoolSize(cpuCores * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("doc-parser-");
        executor.setKeepAliveSeconds(60);

        // 线程池满时由调用者线程执行，实现背压，不丢任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成再关闭，优雅下线
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}