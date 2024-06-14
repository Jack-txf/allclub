package com.feng.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.feng.subject.common.enums.CategoryTypeEnum;
import com.feng.subject.common.enums.IsDeletedFlagEnum;
import com.feng.subject.domain.convert.SubjectCategoryConverter;
import com.feng.subject.domain.entity.SubjectCategoryBO;
import com.feng.subject.domain.service.SbCategoryDomainService;
import com.feng.subject.infra.basic.entity.SubjectCategory;
import com.feng.subject.infra.basic.service.SubjectCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("sbCategoryDomainService")
@Slf4j
public class SbCategoryDomainServiceImpl implements SbCategoryDomainService {
    @Resource
    private SubjectCategoryService subjectCategoryService;
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
        List<SubjectCategoryBO> boList = SubjectCategoryConverter.INSTANCE.convertBoToCategory(categoryList);
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.queryPrimaryCategory.boList:{}",
                    JSON.toJSONString(boList));
        }
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
        List<SubjectCategoryBO> boList = SubjectCategoryConverter.INSTANCE.convertBoToCategory(categoryList);
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
        // SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertBoToCategory(subjectCategoryBO);
        SubjectCategory category = new SubjectCategory();
        category.setId(subjectCategoryBO.getId());
        category.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        int update = subjectCategoryService.update(category);
        return update > 0;
    }
}
