package com.feng.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.feng.subject.common.enums.CategoryTypeEnum;
import com.feng.subject.common.enums.IsDeletedFlagEnum;
import com.feng.subject.domain.convert.SubjectCategoryConverter;
import com.feng.subject.domain.entity.SubjectCategoryBO;
import com.feng.subject.domain.entity.SubjectLabelBO;
import com.feng.subject.domain.service.SbCategoryDomainService;
import com.feng.subject.infra.basic.entity.SubjectCategory;
import com.feng.subject.infra.basic.entity.SubjectLabel;
import com.feng.subject.infra.basic.entity.SubjectMapping;
import com.feng.subject.infra.basic.service.SubjectCategoryService;
import com.feng.subject.infra.basic.service.SubjectLabelService;
import com.feng.subject.infra.basic.service.SubjectMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service("sbCategoryDomainService")
@Slf4j
public class SbCategoryDomainServiceImpl implements SbCategoryDomainService {
    @Resource
    private SubjectCategoryService subjectCategoryService;
    @Resource
    private SubjectMappingService subjectMappingService;
    @Resource
    private SubjectLabelService subjectLabelService;
    @Resource
    private ThreadPoolExecutor labelThreadPool;

    @Override
    public void add(SubjectCategoryBO subjectCategoryBO) {
        // BO 需要转换为 数据库中的实体类
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE
                .convertBoToCategory(subjectCategoryBO);
        subjectCategoryService.insert(subjectCategory);
    }

    @Override
    public List<SubjectCategoryBO> queryPrimaryCategory() {
        // 构建条件~~~~~~~~
        SubjectCategory subjectCategory = new SubjectCategory();
        subjectCategory.setParentId(0L);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode()); // 查询未删除的
        subjectCategory.setCategoryType(CategoryTypeEnum.PRIMARY.getCode()); // 查询大类

        List<SubjectCategory> categoryList = subjectCategoryService.queryPrimaryCategory(subjectCategory);
        // entity转为BO
        List<SubjectCategoryBO> boList = SubjectCategoryConverter.INSTANCE.convertCategoryToBO(categoryList);
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.queryPrimaryCategory.boList:{}",
                    JSON.toJSONString(boList));
        }
        boList.forEach(bo->{
            Integer count = subjectCategoryService.querySubjectCount(bo.getId());
            bo.setCount(count);
        });
        return boList;
    }

    @Override
    public List<SubjectCategoryBO> queryCategory(SubjectCategoryBO subjectCategoryBO) {
        // BO转为entity
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode()); // 查询未删除的
        // 查询出entityList
        List<SubjectCategory> categoryList = subjectCategoryService.queryCategory(subjectCategory);
        // entityList转为BOList
        List<SubjectCategoryBO> boList = SubjectCategoryConverter.INSTANCE.convertCategoryToBO(categoryList);
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.queryPrimaryCategory.boList:{}",
                    JSON.toJSONString(boList));
        }
        return boList;
    }

    // 更新分类
    @Override
    public Boolean update(SubjectCategoryBO subjectCategoryBO) {
        // BO转为entity
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryBO);
        int update = subjectCategoryService.update(subjectCategory);
        return update > 0;
    }

    // 删除分类 实际上只用把is_delete字段更改为1即可
    @Override
    public Boolean delete(SubjectCategoryBO subjectCategoryBO) {
        // BO转为entity
        // SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryBO);
        SubjectCategory category = new SubjectCategory();
        category.setId(subjectCategoryBO.getId());
        category.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        int update = subjectCategoryService.update(category);
        return update > 0;
    }

    /*
    点击一个大类之后，直接查出该大类下的二级分类及其对应的标签，故传过来的categoryId在数据库中，他的parentID必是0，传过来的必定是顶级分类！
    1.顺序查询，遍历二级分类的数组，查询每个二级分类对应的标签
    2.多线程异步查询，并行查询每个二级分类对应的标签
     */
    @Override
    public List<SubjectCategoryBO> queryCategoryAndLabel(SubjectCategoryBO subjectCategoryBO) {
        Long id = subjectCategoryBO.getId(); // categoryId
        SubjectCategory subjectCategory = new SubjectCategory();
        subjectCategory.setParentId(id);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        // 查询出parentID为categoryId的所有二级分类
        List<SubjectCategory> subjectCategoryList = subjectCategoryService.queryCategory(subjectCategory);

        List<SubjectCategoryBO> categoryBOList = SubjectCategoryConverter.INSTANCE.convertCategoryToBO(subjectCategoryList);
        Map<Long, List<SubjectLabelBO>> map = new HashMap<>(); // completableFutures执行完得到的结果
        // 遍历subjectCategoryList，查询每个category下面的标签（多线程）
        List<CompletableFuture<Map<Long, List<SubjectLabelBO>>>> completableFutures = categoryBOList.stream().map(category ->
                CompletableFuture.supplyAsync(() -> getLabelBOList(category), labelThreadPool) // 第一个参数是执行的任务，第二个是线程池
        ).collect(Collectors.toList());
        completableFutures.forEach(future -> {
            try {
                Map<Long, List<SubjectLabelBO>> resultMap = future.get(); //等待获取结果
                if (!CollectionUtils.isEmpty(resultMap)) { // 不空
                    map.putAll(resultMap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 为所有的二级分类设置标签的集合
        categoryBOList.forEach(categoryBO -> {
            if (!CollectionUtils.isEmpty(map.get(categoryBO.getId()))) { // 该categoryId下面的标签不为空
                categoryBO.setLabelBOList(map.get(categoryBO.getId()));
            }
        });
        return categoryBOList;
    }
    /*
    一个categoryID 对应 多个标签（只有一个entry）
     */
    private Map<Long, List<SubjectLabelBO>> getLabelBOList(SubjectCategoryBO category) {
        if (log.isInfoEnabled()) {
            log.info("getLabelBOList:{}", JSON.toJSONString(category));
        }
        Map<Long, List<SubjectLabelBO>> labelMap = new HashMap<>();
        SubjectMapping subjectMapping = new SubjectMapping();
        // 在subject_mapping表中查询category是categoryID的
        subjectMapping.setCategoryId(category.getId());
        List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);
        if (CollectionUtils.isEmpty(mappingList)) {
            return null;
        }
        // 该categoryID下的所有labelId
        List<Long> labelIdList = mappingList.stream().map(SubjectMapping::getLabelId).collect(Collectors.toList());
        List<SubjectLabel> labelList = subjectLabelService.batchQueryById(labelIdList);
        List<SubjectLabelBO> labelBOList = new LinkedList<>();
        labelList.forEach(label -> {
            SubjectLabelBO subjectLabelBO = new SubjectLabelBO();
            subjectLabelBO.setId(label.getId());
            subjectLabelBO.setLabelName(label.getLabelName());
            subjectLabelBO.setCategoryId(label.getCategoryId());
            subjectLabelBO.setSortNum(label.getSortNum());
            labelBOList.add(subjectLabelBO);
        });
        labelMap.put(category.getId(), labelBOList);
        return labelMap;
    }
}
