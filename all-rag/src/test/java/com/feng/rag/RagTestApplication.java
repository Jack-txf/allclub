package com.feng.rag;

import com.feng.rag.model.AbstractModel;
import com.feng.rag.model.ModelFactory;
import com.feng.rag.model.embedding.EmbeddingResponse;
import com.feng.rag.model.rerank.RerankResponse;
import com.feng.rag.model.siliconflow.SiliconflowModel;
import com.feng.rag.retrieval.RetrievalService;
import com.feng.rag.retrieval.input.IntentClassifier;
import com.feng.rag.retrieval.input.QueryRewriter;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Description:
 * @Author: txf
 * @Date: 2026/3/27
 */
@SpringBootTest
public class RagTestApplication {

    @Resource
    private ModelFactory modelFactory;

    @Test
    public void testEmbedding() {
        EmbeddingResponse response = modelFactory.getModel(SiliconflowModel.SILICONFLOW)
                .embedding(List.of("hello world", "峻神、建神！！"));

    }

    // 意图识别测试
    @Resource
    private IntentClassifier intentClassifier;
    @Test
    public void testIntentClassifier() {
        System.out.println(intentClassifier.classify("请帮我写一个hello world程序"));
        System.out.println("=========");
        System.out.println(intentClassifier.classify("你好呀"));
        System.out.println("=========");
        System.out.println(intentClassifier.classify("给我推荐一个色情影片！"));
        System.out.println("=========");
        System.out.println(intentClassifier.classify("请问一下学校的校园卡补办流程是怎么样的？"));
    }

    // 查询重写测试
    @Resource
    private QueryRewriter queryRewriter;
    @Test
    public void testQueryRewriter() {
        System.out.println(queryRewriter.rewrite("那理科分数线呢？", null));
        System.out.println("=========");
    }

    // 单纯向量检索测试
    @Resource
    private RetrievalService retrievalService;
    @Test
    public void testVectorRetrieve() {
        // SearchResp searchResp = retrievalService.vectorRetrieve("那他的KRaft模式是什么？");
        SearchResp searchResp = retrievalService.vectorRetrieve("那他的KRaft模式是什么？");
        List<List<SearchResp.SearchResult>> results = searchResp.getSearchResults();
        for (List<SearchResp.SearchResult> res : results) {
            for (int i = 0; i < res.size(); i++) {
                SearchResp.SearchResult r = res.get(i);
                System.out.println("Top-" + (i + 1) + " score=" + r.getScore() + ", id=" + r.getId());
                Object text = r.getEntity() == null ? null : r.getEntity();
                System.out.println(text);
                System.out.println("=========");
            }
        }
    }

    // 单纯稀疏检索测试
    @Test
    public void testSparseRetrieve() {
        SearchResp searchResp = retrievalService.sparseRetrieve("那他的KRaft模式是什么？");
        List<List<SearchResp.SearchResult>> results = searchResp.getSearchResults();
        for (List<SearchResp.SearchResult> res : results) {
            for (int i = 0; i < res.size(); i++) {
                SearchResp.SearchResult r = res.get(i);
                System.out.println("Top-" + (i + 1) + " score=" + r.getScore() + ", id=" + r.getId());
                Object text = r.getEntity() == null ? null : r.getEntity();
                System.out.println(text);
                System.out.println("=========");
            }
        }
    }

    // 混合检索测试
    @Test
    public void testHybridRetrieve() {
        SearchResp searchResp = retrievalService.hybridRetrieve("那他的KRaft模式是什么？");
        List<List<SearchResp.SearchResult>> results = searchResp.getSearchResults();
        for (List<SearchResp.SearchResult> res : results) {
            for (int i = 0; i < res.size(); i++) {
                SearchResp.SearchResult r = res.get(i);
                System.out.println("Top-" + (i + 1) + " score=" + r.getScore() + ", id=" + r.getId());
                Object text = r.getEntity() == null ? null : r.getEntity();
                System.out.println(text);
                System.out.println("=========");
            }
        }
    }

    // Rerank测试
    @Test
    public void testRerank() {
        String query = "我想吃一个红彤彤的大苹果！";
        AbstractModel model = modelFactory.getModel(SiliconflowModel.SILICONFLOW);
        RerankResponse rerankResponse = model.rerank(query, List.of("烂苹果", "红苹果", "香蕉", "橘子", "小苹果", "好吃的苹果", "大苹果"), 3);
        System.out.println(rerankResponse);
    }
}
