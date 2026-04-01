package com.feng.rag.vector.service.impl;

import com.feng.rag.chunk.model.Chunk;
import com.feng.rag.chunk.model.ChunkingResult;
import com.feng.rag.chunk.service.ChunkingService;
import com.feng.rag.controller.R;
import com.feng.rag.datasource.common.DataSourceRequest;
import com.feng.rag.datasource.common.DocumentParseResult;
import com.feng.rag.datasource.service.DataSourceIngestionService;
import com.feng.rag.model.ModelFactory;
import com.feng.rag.model.embedding.EmbeddingResponse;
import com.feng.rag.model.siliconflow.SiliconflowModel;
import com.feng.rag.vector.config.MilvusProperties;
import com.feng.rag.vector.exception.MilvusException;
import com.feng.rag.vector.service.VectorService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.CollectionInfo;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.request.data.EmbeddedText;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.request.ranker.RRFRanker;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Milvus 服务实现
 *
 * @author txf
 * @since 2026/3/26
 */
@Slf4j
@Service("milvusService")
@RequiredArgsConstructor
public class MilvusServiceImpl implements VectorService {

    private final MilvusClientV2 milvusClient;
    private final MilvusProperties properties;
    private final Gson gson = new Gson();

    private final DataSourceIngestionService ingestionService;
    private final ChunkingService chunkingService;
    private final ModelFactory modelFactory;

    // ==================== 集合管理 ====================
    @Override
    public boolean createCollection(String collectionName, int dimension) {
        try {
            log.info("[Milvus] 创建集合: name={}, dimension={}", collectionName, dimension);
            if (hasCollection(collectionName)) {
                log.warn("[Milvus] 集合已存在: {}", collectionName);
                return true;
            }
            // 构建字段列表
            List<CreateCollectionReq.FieldSchema> fields = buildFieldSchemaList(dimension);
            // 构建索引参数
            List<IndexParam> indexParams = buildIndexParams();
            // 创建集合请求 - 使用 builder 模式
            CreateCollectionReq req = CreateCollectionReq.builder()
                    .collectionName(collectionName)
                    .collectionSchema(CreateCollectionReq.CollectionSchema.builder()
                            .fieldSchemaList(fields)
                            .enableDynamicField(true)
                            .functionList(List.of(buildFunction()))
                            .build())
                    .indexParams(indexParams)
                    .numShards(properties.getCollection().getShardsNum())
                    .build();

            milvusClient.createCollection(req);
            log.info("[Milvus] 集合创建成功: {}", collectionName);
            return true;

        } catch (Exception e) {
            log.error("[Milvus] 创建集合失败: {}", collectionName, e);
            throw new MilvusException("CREATE_COLLECTION", "创建集合失败: " + e.getMessage(), e);
        }
    }
    // ==================== 私有辅助方法 ====================
    /**
     * 构建字段 Schema 列表
     */
    private List<CreateCollectionReq.FieldSchema> buildFieldSchemaList(int dimension) {
        List<CreateCollectionReq.FieldSchema> fields = new ArrayList<>();
        // ID 字段，文档ID！！！
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(properties.getCollection().getIdField())
                .dataType(DataType.VarChar)
                .isPrimaryKey(true)
                .maxLength(36).description("ID主键")
                .build());
        // 分块--该文档的分块索引 【doc_id + chunk_index 可以快速定位是哪一个文档里面的分块】
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("chunk_index")
                .dataType(DataType.Int32)
                .isNullable(true)
                .build());
        // 租户/组织 ID：【企业级核心】利用 Partition Key 实现多租户数据物理隔离
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("org_id")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .isPartitionKey(true).description("组织机构ID，用于多租户查询加速")
                .build());
        // 向量字段
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(properties.getCollection().getVectorField())
                .dataType(DataType.FloatVector)
                .dimension(dimension) // 向量维度，记得要和使用的embedding模型的维度要一致！！！！！！！
                .build());
        // 内容字段（支持全文搜索）【稀疏检索，原有的 contentField (VarChar) 必须开启分词器支持】
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(properties.getCollection().getContentField())
                .dataType(DataType.VarChar)
                .enableAnalyzer(true) // 重点：开启分析器（分词）
                .analyzerParams(Map.of("type", "chinese"))
                .maxLength(65535).description("内容字段，原文内容")
                .build());
        // 元数据字段
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(properties.getCollection().getMetadataField())
                .dataType(DataType.JSON)
                .maxLength(4096).isNullable(true)
                .build());
        // 1. 字段定义：增加“稀疏向量仓”【稀疏检索】
        // 增加一个 SparseFloatVector 类型的字段。这个字段不需要你插入数据，它是给 Milvus 内置的 BM25 算法存放“计算结果”的。
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("sparse_vector")
                .dataType(DataType.SparseFloatVector)
                .build());
        return fields;
    }

    /**
     * 构建索引参数
     */
    private List<IndexParam> buildIndexParams() {
        List<IndexParam> params = new ArrayList<>();
        // 向量索引
        IndexParam.IndexParamBuilder<?, ?> indexBuilder = IndexParam.builder()
                .fieldName(properties.getCollection().getVectorField())
                .indexName(properties.getCollection().getVectorField() + "_idx")
                .indexType(IndexParam.IndexType.valueOf(properties.getCollection().getIndexType()))
                .metricType(IndexParam.MetricType.valueOf(properties.getCollection().getMetricType()));
        // HNSW 额外参数
        if ("HNSW".equals(properties.getCollection().getIndexType())) {
            java.util.Map<String, Object> extraParams = new java.util.HashMap<>();
            extraParams.put("M", 16);
            extraParams.put("efConstruction", 200);
            indexBuilder.extraParams(extraParams);
        }
        params.add(indexBuilder.build());
        // org_id 标量索引（加速多租户查询）
        params.add(IndexParam.builder()
                .fieldName("org_id")
                .indexName("org_id_idx")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .build());
        // 【稀疏检索】 索引定义：增加 BM25 专用索引
        params.add(IndexParam.builder()
                .fieldName("sparse_vector") // 对应上面的稀疏向量字段
                .indexName("sparse_index")
                .indexType(IndexParam.IndexType.SPARSE_INVERTED_INDEX) // 稀疏倒排索引
                .metricType(IndexParam.MetricType.BM25) // 必选 BM25
                .build());
        return params;
    }

    /**
     * function 在 createCollection 方法中，你需要定义一个 Function，它像一个“触发器”：
     * 每当你存入一段文本，它就自动算出 BM25 分数并填入稀疏向量字段。
     * @return
     */
    private CreateCollectionReq.Function buildFunction() {
        return CreateCollectionReq.Function.builder()
                .name("text_bm25_gen")
                .functionType(FunctionType.BM25)
                .inputFieldNames(Collections.singletonList(properties.getCollection().getContentField())) // 输入：你的内容文本
                .outputFieldNames(Collections.singletonList("sparse_vector")) // 输出：自动填入稀疏向量字段
                .build();
    }

    @Override
    public boolean createCollection() {
        return createCollection(
                properties.getCollection().getName(),
                properties.getCollection().getDimension()
        );
    }

    @Override
    public boolean dropCollection(String collectionName) {
        try {
            log.info("[Milvus] 删除集合: {}", collectionName);

            if (!hasCollection(collectionName)) {
                log.warn("[Milvus] 集合不存在: {}", collectionName);
                return true;
            }

            milvusClient.dropCollection(DropCollectionReq.builder()
                    .collectionName(collectionName)
                    .build());

            log.info("[Milvus] 集合删除成功: {}", collectionName);
            return true;

        } catch (Exception e) {
            log.error("[Milvus] 删除集合失败: {}", collectionName, e);
            throw new MilvusException("DROP_COLLECTION", "删除集合失败: " + e.getMessage(), e);
        }
    }


    @Override
    public boolean hasCollection(String collectionName) {
        try {
            return milvusClient.hasCollection(HasCollectionReq.builder()
                    .collectionName(collectionName)
                    .build());
        } catch (Exception e) {
            log.error("[Milvus] 检查集合存在失败: {}", collectionName, e);
            return false;
        }
    }

    @Override
    public List<String> listCollections() {
        try {
            ListCollectionsResp resp = milvusClient.listCollections();
            return resp.getCollectionInfos().stream()
                    .map(CollectionInfo::getCollectionName)
                    .toList();
        } catch (Exception e) {
            log.error("[Milvus] 获取集合列表失败", e);
            throw new MilvusException("LIST_COLLECTIONS", "获取集合列表失败", e);
        }
    }
    // 【重要方法】
    @Override
    public R tackleFile(MultipartFile file, String org_id) {
        long startTime = System.currentTimeMillis();
        String collectionName = properties.getCollection().getName();
        try {
            // 1. 确保集合存在
            if (!hasCollection(collectionName)) {
                log.info("[tackleFile] 集合不存在，创建默认集合: {}", collectionName);
                createCollection();
            }
            // 2. 文件解析
            log.info("[tackleFile] 开始解析文件: {}, org_id: {}", file.getOriginalFilename(), org_id);
            DataSourceRequest request = new DataSourceRequest();
            request.setSourceType(DataSourceRequest.SourceType.FILE_UPLOAD);
            request.setFile(file);
            request.setSourceId(org_id + "_" + file.getOriginalFilename());
            DocumentParseResult result = ingestionService.ingest(request);
            if (result.getStatus() != DocumentParseResult.ParseStatus.SUCCESS) {
                log.error("[tackleFile] 文件解析失败: {}", result.getParseErrors());
                return R.error("文件解析失败: " + result.getParseErrors());
            }

            // 3. 分块
            log.info("[tackleFile] 开始分块，文档: {}", result.getFileName());
            ChunkingResult chunkRes = chunkingService.chunk(result);
            if (chunkRes.getStatus() != ChunkingResult.Status.SUCCESS || chunkRes.getChunks().isEmpty()) {
                log.error("[tackleFile] 分块失败或无内容");
                return R.error("分块失败或无内容");
            }
            List<Chunk> chunks = chunkRes.getChunks();
            log.info("[tackleFile] 分块完成，共 {} 个分块", chunks.size());

            // 4. 分批向量化并存储（每批 30 条）
            int batchSize = 30;
            int totalInserted = 0;

            for (int i = 0; i < chunks.size(); i += batchSize) {
                List<Chunk> batch = chunks.subList(i, Math.min(i + batchSize, chunks.size()));
                // 4.1 获取 embedding
                List<String> texts = batch.stream()
                    .map(Chunk::getContent)
                    .toList();
                EmbeddingResponse embeddingRes = modelFactory
                        .getModel(SiliconflowModel.SILICONFLOW).embedding(texts);
                if (embeddingRes.getData() == null || embeddingRes.getData().isEmpty()) {
                    log.warn("[tackleFile] 第 {} 批向量化失败，跳过", i / batchSize);
                    continue;
                }
                // 4.2 构建 Milvus 数据 (使用 JsonObject)
                List<JsonObject> dataList = new ArrayList<>();
                for (int j = 0; j < batch.size(); j++) {
                    Chunk chunk = batch.get(j);
                    String docId = UUID.randomUUID().toString();
                    // 元数据
                    JsonObject metadata = new JsonObject();
                    metadata.addProperty("source", result.getFileName());
                    metadata.addProperty("mimeType", result.getMimeType());
                    metadata.addProperty("chunkIndex", chunk.getChunkIndex());
                    metadata.addProperty("documentId", chunk.getDocumentId());
                    metadata.addProperty("startOffset", chunk.getStartOffset());
                    metadata.addProperty("endOffset", chunk.getEndOffset());
                    // 构建向量字段
                    List<Float> vector = embeddingRes.getData().get(j).getEmbedding();
                    com.google.gson.JsonArray vectorArray = new com.google.gson.JsonArray();
                    for (Float v : vector) {
                        vectorArray.add(v);
                    }

                    JsonObject row = new JsonObject();
                    row.addProperty(properties.getCollection().getIdField(), docId);
                    row.add(properties.getCollection().getVectorField(), vectorArray);
                    row.addProperty(properties.getCollection().getContentField(), chunk.getContent());
                    row.add(properties.getCollection().getMetadataField(), metadata);
                    row.addProperty("chunk_index", chunk.getChunkIndex());
                    row.addProperty("org_id", org_id);
                    dataList.add(row);
                }
                // 4.3 插入 Milvus
                InsertReq insertReq = InsertReq.builder()
                    .collectionName(collectionName)
                    .data(dataList)
                    .build();
                milvusClient.insert(insertReq);
                totalInserted += dataList.size();
                log.info("[tackleFile] 第 {} 批插入成功，数量: {}", (i / batchSize) + 1, dataList.size());
            }
            long duration = System.currentTimeMillis() - startTime;
            log.info("[tackleFile] 文件处理完成，插入 {} 条记录，耗时 {} ms", totalInserted, duration);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("totalChunks", chunks.size());
            resultMap.put("inserted", totalInserted);
            resultMap.put("durationMs", duration);
            resultMap.put("collection", collectionName);
            return R.ok().data(resultMap);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            // 处理维度不匹配问题：删除旧集合并重新创建
            if (errorMsg != null && errorMsg.contains("Incorrect dimension for field 'vector'")) {
                log.warn("[tackleFile] 集合维度不匹配，删除并重建集合: {}", collectionName);
                try {
                    dropCollection(collectionName);
                    createCollection();
                    // 重新执行本方法
                    return tackleFile(file, org_id);
                } catch (Exception recreateEx) {
                    log.error("[tackleFile] 重建集合失败", recreateEx);
                    return R.error("重建集合失败: " + recreateEx.getMessage());
                }
            }
            log.error("[tackleFile] 处理文件失败: {}", file.getOriginalFilename(), e);
            return R.error("处理文件失败: " + e.getMessage());
        }
    }

    /**
     * 混合检索
     */
    @Override
    public SearchResp hybridSearch(String query, Integer topK, String orgId, String collection) {
        String collectionName = (collection != null && !collection.isEmpty())
                ? collection
                : properties.getCollection().getName();
        int limit = (topK != null && topK > 0) ? topK : 10;
        String targetOrgId = (orgId != null && !orgId.isEmpty()) ? orgId : "default";
        log.info("[sparseSearch] 开始混合检索: query={}, topK={}, collection={}",
                query.substring(0, Math.min(query.length(), 50)), limit, collectionName);
        try {
            // 执行 Milvus 混合检索
            return executeHybridSearch(query, limit, targetOrgId, collectionName);
        } catch (Exception e) {
            log.error("[sparseSearch] 混合检索失败", e);
            throw new MilvusException("SPARSE_SEARCH", "混合检索失败: " + e.getMessage(), e);
        }
    }
    private SearchResp executeHybridSearch(String query, int limit, String targetOrgId, String collectionName) {
        // 1. 得到用户query的Embedding
        EmbeddingResponse response = modelFactory.getModel(SiliconflowModel.SILICONFLOW).embedding(List.of(query));
        List<Float> embedding = response.getData().getFirst().getEmbedding();
        // 2. 向量检索
        AnnSearchReq denseReq = AnnSearchReq.builder()
                .vectorFieldName("vector")
                .vectors(Collections.singletonList(new FloatVec(embedding)))
                .params("{\"nprobe\": " + 50 + "}")
                .topK(limit)
                .build();
        // 3. 稀疏检索
        AnnSearchReq sparseReq = AnnSearchReq.builder()
                .vectorFieldName("sparse_vector")
                .vectors(Collections.singletonList(new EmbeddedText(query)))
                .params("{\"drop_ratio_search\": " +  0.3 + "}")
                .topK(limit)
                .build();
        // 4.融合
        CreateCollectionReq.Function rerank = CreateCollectionReq.Function.builder()
                .name("rrf")
                .functionType(FunctionType.RERANK)
                .param("reranker", "rrf")
                .param("k", "100")
                .build();
        HybridSearchReq hybridReq = HybridSearchReq.builder()
                .collectionName(collectionName)
                .searchRequests(List.of(denseReq, sparseReq))
                // .ranker(rerank) // 这个不推荐了
                .functionScore(FunctionScore.builder()
                        .addFunction(rerank)
                        .build())
                .topK(limit)
                .consistencyLevel(ConsistencyLevel.EVENTUALLY)
                .outFields(List.of(properties.getCollection().getIdField(),
                        "chunk_index",
                        "org_id",
                        properties.getCollection().getContentField(),
                        properties.getCollection().getMetadataField()
                ))
                .build();
        // 5. 执行
        return milvusClient.hybridSearch(hybridReq);
    }

    /**
     * 稀疏检索（基于关键词匹配）
     * 使用 Milvus 的 query 接口配合 like 表达式进行关键词匹配
     * @param query      查询文本
     * @param topK       返回结果数量
     * @param orgId      组织ID（多租户隔离）
     * @param collection 集合名称（可选，默认使用配置中的）
     * @return 搜索结果列表
     */
    @Override
    public SearchResp sparseSearch(String query, Integer topK, String orgId, String collection) {
        String collectionName = (collection != null && !collection.isEmpty())
                ? collection
                : properties.getCollection().getName();
        int limit = (topK != null && topK > 0) ? topK : 10;
        String targetOrgId = (orgId != null && !orgId.isEmpty()) ? orgId : "default";
        log.info("[sparseSearch] 开始稀疏检索: query={}, topK={}, collection={}",
                query.substring(0, Math.min(query.length(), 50)), limit, collectionName);
        try {
            // 执行 Milvus 稀疏检索
            return executeSparseSearch(query, limit, targetOrgId, collectionName);
        } catch (Exception e) {
            log.error("[sparseSearch] 稀疏检索失败", e);
            throw new MilvusException("SPARSE_SEARCH", "稀疏检索失败: " + e.getMessage(), e);
        }
    }
    private SearchResp executeSparseSearch(String query, int limit, String targetOrgId, String collectionName) {
        Map<String, Object> params = new HashMap<>();
        params.put("metric_type", "BM25");
        params.put("drop_ratio_search", 0.3);

        return milvusClient.search(SearchReq.builder()
                .collectionName(collectionName)
                .annsField("sparse_vector") // 对应定义的 SparseFloatVector 字段
                .data(Collections.singletonList(new EmbeddedText(query)))
                .topK(limit)
                .searchParams(params)
                .consistencyLevel(ConsistencyLevel.EVENTUALLY)
                .outputFields(List.of(properties.getCollection().getIdField(),
                        "chunk_index",
                        "org_id",
                        properties.getCollection().getContentField(),
                        properties.getCollection().getMetadataField()
                ))
                .build());
    }


    /**
     * 向量检索
     * @param query      查询文本
     * @param topK       返回结果数量
     * @param orgId      组织ID（多租户隔离）
     * @param collection 集合名称（可选，默认使用配置中的）
     * @return 搜索结果列表
     */
    @Override
    public SearchResp vectorSearch(String query, Integer topK, String orgId, String collection) {
        String collectionName = (collection != null && !collection.isEmpty())
                ? collection
                : properties.getCollection().getName();
        int limit = (topK != null && topK > 0) ? topK : 10;
        String targetOrgId = (orgId != null && !orgId.isEmpty()) ? orgId : "default";
        log.info("[vectorSearch] 开始向量检索: query={}, topK={}, collection={}",
                query.substring(0, Math.min(query.length(), 50)), limit, collectionName);
        try {
            // 1. 向量化查询文本
            EmbeddingResponse embeddingRes = modelFactory
                    .getModel(SiliconflowModel.SILICONFLOW)
                    .embedding(List.of(query));
            if (embeddingRes.getData() == null || embeddingRes.getData().isEmpty()) {
                log.error("[vectorSearch] 查询文本向量化失败");
                return null;
            }
            List<Float> queryVector = embeddingRes.getData()
                    .getFirst().getEmbedding();
            // 2. 执行 Milvus 向量搜索
            return executeVectorSearch(queryVector, limit, targetOrgId, collectionName);
        } catch (Exception e) {
            log.error("[vectorSearch] 向量检索失败", e);
            throw new MilvusException("VECTOR_SEARCH", "向量检索失败: " + e.getMessage(), e);
        }
    }
    /**
     * 执行 Milvus 向量搜索
     */
    private SearchResp executeVectorSearch(List<Float> queryVector, int topK,
                                                    String orgId, String collectionName) {
        Map<String, Object> params = new HashMap<>();
        params.put("metric_type", "COSINE");
        params.put("nprobe", 50);

        return milvusClient.search(SearchReq.builder()
                .collectionName(collectionName)
                .annsField(properties.getCollection().getVectorField())
                .data(Collections.singletonList(new FloatVec(queryVector)))
                .topK(topK)
                .searchParams(params)
                .consistencyLevel(ConsistencyLevel.EVENTUALLY)
                .outputFields(List.of(properties.getCollection().getIdField(),
                        "chunk_index",
                        properties.getCollection().getContentField(),
                        properties.getCollection().getMetadataField(),
                        "org_id"
                        ))
                .build());
    }
}
