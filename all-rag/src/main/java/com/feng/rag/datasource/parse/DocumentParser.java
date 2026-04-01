package com.feng.rag.datasource.parse;


import com.feng.rag.datasource.common.DocumentParseResult;

import java.io.InputStream;

/**
 * 文档解析器统一接口
 */
public interface DocumentParser {

    /**
     * 解析文档，提取纯文本内容和元数据。
     *
     * @param inputStream 文档输入流，实现类不应关闭此流
     * @param fileName    原始文件名（含扩展名），用于元数据记录和日志
     * @param sourceId    业务来源标识，透传到解析结果
     * @return 解析结果，永远不返回 null（失败时 status = FAILED）
     */
    DocumentParseResult parse(InputStream inputStream, String fileName, String sourceId);

    /**
     * 声明此解析器支持的 MIME 类型列表。
     *
     * <p>工厂通过此方法建立 MIME → 解析器的路由表。
     * 返回的列表应为不可变集合。
     *
     * @return 支持的 MIME 类型，如 ["application/pdf"]
     */
    java.util.List<String> supportedMimeTypes();

    /**
     * 解析器名称，用于日志和 metrics 标记。
     *
     * @return 可读名称，如 "TikaPdfParser"
     */
    default String parserName() {
        return this.getClass().getSimpleName();
    }
}