package com.feng.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.feng.subject.common.enums.IsDeletedFlagEnum;
import com.feng.subject.domain.convert.SubjectInfoConverter;
import com.feng.subject.domain.entity.SubjectInfoBO;
import com.feng.subject.domain.service.SubjectInfoDomainService;
import com.feng.subject.domain.strategy.SubjectTypeHandlerFactory;
import com.feng.subject.infra.basic.entity.SubjectInfo;
import com.feng.subject.infra.basic.entity.SubjectMapping;
import com.feng.subject.infra.basic.service.SubjectInfoService;
import com.feng.subject.infra.basic.service.SubjectMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Service("subjectInfoDomainService")
@Slf4j
public class SubjectInfoDomainServiceImpl implements SubjectInfoDomainService {
    @Resource
    private SubjectInfoService subjectInfoService;
    @Resource
    private SubjectTypeHandlerFactory subjectTypeHandlerFactory;
    @Resource
    private SubjectMappingService subjectMappingService;

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        if ( log.isInfoEnabled() ) {
            log.info("info.SubjectInfoDomainServiceImpl.add: {}", JSON.toJSONString(subjectInfoBO));
        }
        /*
        !!!!
        1. 传统用if判断添加的题目的类型，if会很多.
        2. 采用工厂+策略模式添加题目
         */
        // 实现步骤
        // 1.插入题目信息表的主表（题目大致信息）
        SubjectInfo subjectInfo = SubjectInfoConverter.INSTANCE.convertBoToInfo(subjectInfoBO);
        subjectInfoService.insert(subjectInfo); // 主表插入完成
        // 2.插入题目具体类型的表
        subjectTypeHandlerFactory.getHandler(subjectInfoBO.getSubjectType()).add(subjectInfoBO);
        // 3.插入题目与标签的对应关系（多对多）
        List<Integer> categoryIds = subjectInfoBO.getCategoryIds(); // 这个题目有哪些分类
        List<Integer> labelIds = subjectInfoBO.getLabelIds(); // 这个题目有哪些标签
        List<SubjectMapping> mappingList = new LinkedList<>();
        categoryIds.forEach(categoryId -> {
            labelIds.forEach(labelId -> {
                SubjectMapping subjectMapping = new SubjectMapping();
                subjectMapping.setSubjectId(subjectInfo.getId());
                subjectMapping.setCategoryId(Long.valueOf(categoryId));
                subjectMapping.setLabelId(Long.valueOf(labelId));
                subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
                mappingList.add(subjectMapping);
            });
        });
        subjectMappingService.batchInsert(mappingList);
    }
}
