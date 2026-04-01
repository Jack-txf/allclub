package com.feng.rag.chunk.strategy;

import com.feng.rag.chunk.model.Chunk;
import com.feng.rag.chunk.model.ChunkingOptions;
import com.feng.rag.chunk.model.ChunkingResult;
import com.feng.rag.datasource.common.DocumentParseResult;

import java.util.List;

/**
 * 分块策略接口 —— 定义文档分块的标准契约
 */
public interface ChunkingStrategy {

    /**
     * 执行文档分块
     *
     * @param document 解析后的文档结果
     * @param options  分块选项
     * @return 分块结果（包含分块列表和统计信息）
     */
    ChunkingResult chunk(DocumentParseResult document, ChunkingOptions options);

    /**
     * 对纯文本进行分块（无需完整 DocumentParseResult）
     *
     * @param text        原始文本内容
     * @param documentId  文档标识
     * @param documentName 文档名称
     * @param options     分块选项
     * @return 分块结果
     */
    ChunkingResult chunkText(String text, String documentId, String documentName, ChunkingOptions options);

    /**
     * 策略名称（用于日志、监控和配置）
     *
     * @return 策略的唯一标识名
     */
    String getStrategyName();

    /**
     * 策略描述
     *
     * @return 策略的功能描述和适用场景
     */
    String getStrategyDescription();

    /**
     * 获取策略的默认选项
     *
     * @return 默认配置
     */
    ChunkingOptions getDefaultOptions();

    /**
     * 检查策略是否适用于给定文档
     *
     * @param document 文档解析结果
     * @return true 如果该策略适合处理此文档
     */
    default boolean isApplicable(DocumentParseResult document) {
        return true;
    }

    /**
     * 获取策略优先级（用于自动策略选择）
     * 数值越高，优先级越高
     *
     * @return 优先级（0-100）
     */
    default int getPriority() {
        return 50;
    }

    /**
     * 策略适用的 MIME 类型列表
     * 返回空列表表示适用所有类型
     *
     * @return MIME 类型列表
     */
    default List<String> getSupportedMimeTypes() {
        return List.of();
    }

    /**
     * 重新分块（基于已有分块进行进一步处理）
     * 用于分块管道组合场景
     *
     * @param chunks  已有分块列表
     * @param options 新的分块选项
     * @return 重新分块后的结果
     */
    default ChunkingResult rechunk(List<Chunk> chunks, ChunkingOptions options) {
        // 默认实现：将分块内容合并后重新分块
        StringBuilder combined = new StringBuilder();
        for (Chunk chunk : chunks) {
            if (chunk.getContent() != null) {
                combined.append(chunk.getContent()).append("\n\n");
            }
        }
        String firstDocId = chunks.isEmpty() ? "unknown" : chunks.get(0).getDocumentId();
        String firstDocName = chunks.isEmpty() ? "unknown" : chunks.get(0).getDocumentName();
        return chunkText(combined.toString(), firstDocId, firstDocName, options);
    }
}
