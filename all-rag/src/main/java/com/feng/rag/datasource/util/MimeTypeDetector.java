package com.feng.rag.datasource.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * MIME 类型检测工具
 *
 * <p>企业级 RAG 系统中，不能依赖文件扩展名判断格式——攻击者可以
 * 将可执行文件改名为 .pdf 上传。必须读取文件头（Magic Bytes）来
 * 确定真实 MIME 类型。
 */
@Slf4j
@Component
public class MimeTypeDetector {

    /**
     * Tika 门面，仅用于 MIME 检测，不做解析。
     * 线程安全，单例复用。
     */
    private final Tika tika;

    public MimeTypeDetector(Tika tika) {
        this.tika = tika;
    }

    /**
     * 检测 MultipartFile 的真实 MIME 类型。
     */
    public String detect(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            /*
             * tika.detect(stream, name) 同时使用文件内容和文件名：
             * - stream：读取文件头（不超过 64KB，读完关闭不影响外部）
             * - name：文件名，仅作辅助（当内容检测不确定时加权）
             */
            String mimeType = tika.detect(is, file.getOriginalFilename());
            log.debug("[MimeDetector] 文件 [{}] 检测到 MIME 类型: {}", file.getOriginalFilename(), mimeType);
            return mimeType;
        } catch (IOException e) {
            log.warn("[MimeDetector] MIME 检测失败，返回默认类型: file={}, error={}",
                file.getOriginalFilename(), e.getMessage());
            return "application/octet-stream";
        }
    }

    /**
     * 检测 InputStream 的 MIME 类型。
     */
    public String detect(InputStream inputStream, String fileName) {
        try {
            return tika.detect(inputStream, fileName);
        } catch (IOException e) {
            log.warn("[MimeDetector] InputStream MIME 检测失败: fileName={}, error={}", fileName, e.getMessage());
            return "application/octet-stream";
        }
    }

    /**
     * 根据 MIME 类型获取推荐的文件扩展名。
     */
    public String getExtension(String mimeType) {
        try {
            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            MimeType type = allTypes.forName(mimeType);
            return type.getExtension();
        } catch (MimeTypeException e) {
            return ".bin";
        }
    }

    /**
     * 判断 MIME 类型是否为文本类型（text/*）。
     */
    public boolean isTextType(String mimeType) {
        return mimeType != null && mimeType.startsWith("text/");
    }

    /**
     * 判断 MIME 类型是否为 PDF。
     */
    public boolean isPdf(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    /**
     * 判断 MIME 类型是否为 Office 文档（Word/PPT/Excel 新旧格式）。
     */
    public boolean isOfficeDocument(String mimeType) {
        if (mimeType == null) return false;
        return mimeType.startsWith("application/vnd.openxmlformats-officedocument") ||
               mimeType.startsWith("application/vnd.ms-") ||
               "application/msword".equals(mimeType);
    }
}