package com.feng.rag.vector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Milvus 配置属性
 *
 * @author txf
 * @since 2026/3/26
 */
@Data
// @Component
@ConfigurationProperties(prefix = "rag.vector.milvus")
public class MilvusProperties {

    /**
     * 是否启用 Milvus
     */
    private boolean enabled = true;

    /**
     * Milvus 服务地址
     */
    private String uri = "http://localhost:19530";

    /**
     * 认证令牌（可选）
     */
    private String token;

    /**
     * 连接超时时间（毫秒）
     */
    private long connectTimeoutMs = 10000;

    /**
     * 请求超时时间（毫秒）
     */
    private long requestTimeoutMs = 30000;

    /**
     * 数据库名称
     */
    private String databaseName = "default";

    /**
     * 默认集合配置
     */
    private CollectionConfig collection = new CollectionConfig();

    /**
     * 集合配置
     */
    @Data
    public static class CollectionConfig {
        /**
         * 默认集合名称
         */
        private String name = "rag_documents";

        /**
         * 向量维度
         */
        private int dimension = 2560;

        /**
         * 向量字段名称
         */
        private String vectorField = "vector";

        /**
         * ID 字段名称
         */
        private String idField = "doc_id";

        /**
         * 内容字段名称
         */
        private String contentField = "content";

        /**
         * 元数据字段名称
         */
        private String metadataField = "metadata";

        /**
         * 分片数量
         */
        private int shardsNum = 1;

        /**
         * 索引类型
         * IVF_FLAT | IVF_SQ8 | IVF_PQ | HNSW | DISKANN | FLAT
         */
        private String indexType = "HNSW";

        /**
         * 度量类型
         * L2 | IP | COSINE
         */
        private String metricType = "COSINE";
    }
}
