package com.feng.rag.vector.service;

import com.feng.rag.controller.R;
import com.feng.rag.vector.entity.SearchResult;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Milvus 向量数据库服务接口
 *
 * @author txf
 * @since 2026/3/26
 */
public interface VectorService {

    /**
     * 创建集合（如果不存在）
     * @param collectionName 集合名称
     * @param dimension      向量维度
     * @return 是否创建成功
     */
    boolean createCollection(String collectionName, int dimension);

    /**
     * 使用默认配置创建集合
     * @return 是否创建成功
     */
    boolean createCollection();

    /**
     * 删除集合
     * @param collectionName 集合名称
     * @return 是否删除成功
     */
    boolean dropCollection(String collectionName);

    /**
     * 检查集合是否存在
     * @param collectionName 集合名称
     * @return 是否存在
     */
    boolean hasCollection(String collectionName);

    /**
     * 获取所有集合名称
     * @return 集合名称列表
     */
    List<String> listCollections();
    // 上传文件，分块，然后向量化，存入milvus
    R tackleFile(MultipartFile file, String org_id);

    // ==================== 向量检索 ====================
    /**
     * 向量检索
     * @param query      查询文本
     * @param topK       返回结果数量
     * @param orgId      组织ID（多租户隔离）
     * @param collection 集合名称（可选，默认使用配置中的）
     * @return 搜索结果列表
     */
    SearchResp vectorSearch(String query, Integer topK, String orgId, String collection);

    // ==================== 稀疏检索 ====================
    /**
     * 稀疏检索（基于关键词匹配）
     * @param query      查询文本
     * @param topK       返回结果数量
     * @param orgId      组织ID（多租户隔离）
     * @param collection 集合名称（可选，默认使用配置中的）
     * @return 搜索结果列表
     */
    SearchResp sparseSearch(String query, Integer topK, String orgId, String collection);

    // ==================== 混合检索 ====================
    /**
     * 混合检索（基于关键词匹配）
     * @param query      查询文本
     * @param topK       返回结果数量
     * @param orgId      组织ID（多租户隔离）
     * @param collection 集合名称（可选，默认使用配置中的）
     * @return 搜索结果列表
     */
    SearchResp hybridSearch(String query, Integer topK, String orgId, String collection);


}
