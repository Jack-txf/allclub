package com.feng.subject.infra.basic.esservice;

import com.feng.subject.common.eneity.PageResult;
import com.feng.subject.common.enums.SubjectInfoTypeEnum;
import com.feng.subject.infra.basic.es.EsIndexInfo;
import com.feng.subject.infra.basic.es.EsRestClientUtil;
import com.feng.subject.infra.basic.es.EsSearchRequest;
import com.feng.subject.infra.basic.es.EsSourceData;
import com.feng.subject.infra.basic.espojo.EsSubjectFields;
import com.feng.subject.infra.basic.espojo.SubjectInfoEs;
import org.apache.commons.collections4.MapUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service("esSubjectService")
public class EsSubjectServiceImpl implements EsSubjectService{
    @Override
    public boolean insert(SubjectInfoEs subjectInfoEs) {
        EsSourceData esSourceData = new EsSourceData();
        Map<String, Object> data = convert2EsToSourceData(subjectInfoEs); //将要插入es的对象转换为map的关系映射
        esSourceData.setDocId(subjectInfoEs.getDocId().toString());
        esSourceData.setData(data); // 构建插入es的source对象
        return EsRestClientUtil.insertDoc(getEsIndexInfo(), esSourceData);
    }
    //将要插入es的对象转换为map的关系映射
    private Map<String, Object> convert2EsToSourceData(SubjectInfoEs subjectInfoEs) {
        Map<String, Object> data = new HashMap<>();
        data.put(EsSubjectFields.SUBJECT_ID, subjectInfoEs.getSubjectId());
        data.put(EsSubjectFields.DOC_ID, subjectInfoEs.getDocId());
        data.put(EsSubjectFields.SUBJECT_NAME, subjectInfoEs.getSubjectName());
        data.put(EsSubjectFields.SUBJECT_ANSWER, subjectInfoEs.getSubjectAnswer());
        data.put(EsSubjectFields.SUBJECT_TYPE, subjectInfoEs.getSubjectType());
        data.put(EsSubjectFields.CREATE_USER, subjectInfoEs.getCreateUser());
        data.put(EsSubjectFields.CREATE_TIME, subjectInfoEs.getCreateTime());
        return data;
    }

    // 获取索引信息
    private EsIndexInfo getEsIndexInfo() {
        EsIndexInfo esIndexInfo = new EsIndexInfo();
        esIndexInfo.setClusterName("dbcee53dd3da"); // 结点名称
        esIndexInfo.setIndexName("subject_index"); // 索引名称
        return esIndexInfo;
    }

    @Override
    public PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs esInfo) {
        PageResult<SubjectInfoEs> pageResult = new PageResult<>();
        EsSearchRequest esSearchRequest = createSearchListQuery(esInfo); // 根据传过来的参数创建自定义的请求
        // (索引信息， 自定义的搜索请求)
        SearchResponse searchResponse = EsRestClientUtil.searchWithTermQuery(getEsIndexInfo(), esSearchRequest);
        // 处理查询结果
        List<SubjectInfoEs> subjectInfoEsList = new LinkedList<>();
        SearchHits searchHits = searchResponse.getHits();
        if (searchHits == null || searchHits.getHits() == null) { // 1.没有结果
            pageResult.setPageNo(esInfo.getPageNo());
            pageResult.setPageSize(esInfo.getPageSize());
            pageResult.setRecords(subjectInfoEsList);
            pageResult.setTotal(0);
            return pageResult; // 直接返回
        }
        // 2.有结果
        SearchHit[] hits = searchHits.getHits(); //查询命中结果集
        for (SearchHit hit : hits) {
            SubjectInfoEs subjectInfoEs = convertHitToSubjectInfo(hit); //将SearchHit转换为我们自定义的对象
            if (Objects.nonNull(subjectInfoEs)) { // 不空，就添加到结果集里面
                subjectInfoEsList.add(subjectInfoEs);
            }
        }
        pageResult.setPageNo(esInfo.getPageNo());
        pageResult.setPageSize(esInfo.getPageSize());
        pageResult.setRecords(subjectInfoEsList);
        pageResult.setTotal(Long.valueOf(searchHits.getTotalHits().value).intValue());
        return pageResult;
    }

    // SearchHit 转换为 esInfo
    private SubjectInfoEs convertHitToSubjectInfo( SearchHit hit) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap(); //从SearchHit获取map
        if (CollectionUtils.isEmpty(sourceAsMap)) {
            return null;
        }
        // 设置返回结果属性值
        SubjectInfoEs result = new SubjectInfoEs();
        result.setSubjectId(MapUtils.getLong(sourceAsMap, EsSubjectFields.SUBJECT_ID));
        result.setSubjectName(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_NAME));
        result.setSubjectAnswer(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_ANSWER));
        result.setDocId(MapUtils.getLong(sourceAsMap, EsSubjectFields.DOC_ID));
        result.setSubjectType(MapUtils.getInteger(sourceAsMap, EsSubjectFields.SUBJECT_TYPE));
        result.setScore(new BigDecimal(String.valueOf(hit.getScore())).multiply(new BigDecimal("100.00")
                .setScale(2, RoundingMode.HALF_UP)));
        // 处理高亮===============
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        HighlightField subjectNameField = highlightFields.get(EsSubjectFields.SUBJECT_NAME);
        if(Objects.nonNull(subjectNameField)){ // 1.题目名字高亮字段不空
            Text[] fragments = subjectNameField.getFragments();
            StringBuilder subjectNameBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectNameBuilder.append(fragment);
            }
            result.setSubjectName(subjectNameBuilder.toString());
        }
        HighlightField subjectAnswerField = highlightFields.get(EsSubjectFields.SUBJECT_ANSWER);
        if(Objects.nonNull(subjectAnswerField)){ // 2.题目答案的高亮字段不空
            Text[] fragments = subjectAnswerField.getFragments();
            StringBuilder subjectAnswerBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectAnswerBuilder.append(fragment);
            }
            result.setSubjectAnswer(subjectAnswerBuilder.toString());
        }
        return result;
    }

    // 创建查询的请求
    private EsSearchRequest createSearchListQuery(SubjectInfoEs req) {
        EsSearchRequest esSearchRequest = new EsSearchRequest();
        BoolQueryBuilder bq = new BoolQueryBuilder();
        MatchQueryBuilder subjectNameQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_NAME, req.getKeyWord());// 字段名，值

        bq.should(subjectNameQueryBuilder);
        subjectNameQueryBuilder.boost(2);

        MatchQueryBuilder subjectAnswerQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_ANSWER, req.getKeyWord());
        bq.should(subjectAnswerQueryBuilder);

        // 简答题 TODO 这里只查询了简答题
        MatchQueryBuilder subjectTypeQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_TYPE, SubjectInfoTypeEnum.BRIEF.getCode());
        bq.must(subjectTypeQueryBuilder);
        bq.minimumShouldMatch(1);

        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
        highlightBuilder.preTags("<span style = \"color:red\">");
        highlightBuilder.postTags("</span>");

        esSearchRequest.setBq(bq);
        esSearchRequest.setHighlightBuilder(highlightBuilder);
        esSearchRequest.setFields(EsSubjectFields.FIELD_QUERY);
        esSearchRequest.setFrom((req.getPageNo() - 1) * req.getPageSize());
        esSearchRequest.setSize(req.getPageSize());
        esSearchRequest.setNeedScroll(false);
        return esSearchRequest;
    }
}
