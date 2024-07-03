package com.feng.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.feng.subject.common.eneity.PageResult;
import com.feng.subject.common.enums.IsDeletedFlagEnum;
import com.feng.subject.common.utils.IdWorkerUtil;
import com.feng.subject.domain.convert.SubjectInfoConverter;
import com.feng.subject.domain.entity.SubjectInfoBO;
import com.feng.subject.domain.entity.SubjectOptionBO;
import com.feng.subject.domain.service.SubjectInfoDomainService;
import com.feng.subject.domain.strategy.SubjectTypeHandler;
import com.feng.subject.domain.strategy.SubjectTypeHandlerFactory;
import com.feng.subject.infra.basic.entity.SubjectInfo;
import com.feng.subject.infra.basic.entity.SubjectLabel;
import com.feng.subject.infra.basic.entity.SubjectMapping;
import com.feng.subject.infra.basic.espojo.SubjectInfoEs;
import com.feng.subject.infra.basic.esservice.EsSubjectService;
import com.feng.subject.infra.basic.service.SubjectInfoService;
import com.feng.subject.infra.basic.service.SubjectLabelService;
import com.feng.subject.infra.basic.service.SubjectMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service("subjectInfoDomainService")
@Slf4j
public class SubjectInfoDomainServiceImpl implements SubjectInfoDomainService {
    @Resource
    private SubjectInfoService subjectInfoService;
    @Resource
    private SubjectLabelService subjectLabelService;
    @Resource
    private SubjectTypeHandlerFactory subjectTypeHandlerFactory;
    @Resource
    private SubjectMappingService subjectMappingService;
    @Resource
    private EsSubjectService esSubjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SubjectInfoBO subjectInfoBO) {
        if ( log.isInfoEnabled() ) {
            log.info("info.SubjectInfoDomainServiceImpl.add: {}", JSON.toJSONString(subjectInfoBO));
        }
        /*
        !!!!两种方式
        1. 传统用if判断添加的题目的类型，if会很多.
        2. 采用工厂+策略模式添加题目
         */
        // 实现步骤
        // 1.插入题目信息表的主表（题目大致信息）
        SubjectInfo subjectInfo = SubjectInfoConverter.INSTANCE.convertBoToInfo(subjectInfoBO);
        subjectInfo.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectInfoService.insert(subjectInfo); // 主表插入完成
        // 2.插入题目具体类型的表
        subjectInfoBO.setId(subjectInfo.getId()); // 把主键设置一下
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
        // 4.题目同步到es里面去
        SubjectInfoEs subjectInfoEs = new SubjectInfoEs();
        subjectInfoEs.setDocId(new IdWorkerUtil(1, 1, 1).nextId());
        subjectInfoEs.setSubjectId(subjectInfo.getId());
        subjectInfoEs.setSubjectAnswer(subjectInfoBO.getSubjectAnswer());
        subjectInfoEs.setCreateTime(new Date().getTime());
        subjectInfoEs.setCreateUser("田小锋");
        subjectInfoEs.setSubjectName(subjectInfo.getSubjectName());
        subjectInfoEs.setSubjectType(subjectInfo.getSubjectType());
        esSubjectService.insert(subjectInfoEs);
    }

    /*
    题目列表 分页查询
     */
    @Override
    public PageResult<SubjectInfoBO> getSubjectPage(SubjectInfoBO subjectInfoBO) {
        PageResult<SubjectInfoBO> pageResult = new PageResult<>();
        pageResult.setPageNo(subjectInfoBO.getPageNo());
        pageResult.setPageSize(subjectInfoBO.getPageSize());
        // 从哪个序号开始的
        int start = (subjectInfoBO.getPageNo() - 1) * subjectInfoBO.getPageSize();
        // BO转entity
        SubjectInfo subjectInfo = SubjectInfoConverter.INSTANCE.convertBoToInfo(subjectInfoBO);
        // 查询
        int count = subjectInfoService.countByCondition(subjectInfo, subjectInfoBO.getCategoryId()
                , subjectInfoBO.getLabelId());
        if (count == 0) { // 空
            return pageResult;
        }
        // 不空
        List<SubjectInfo> subjectInfoList = subjectInfoService.queryPage(subjectInfo, subjectInfoBO.getCategoryId()
                , subjectInfoBO.getLabelId(), start, subjectInfoBO.getPageSize());
        // entityList转boList
        List<SubjectInfoBO> subjectInfoBOS = SubjectInfoConverter.INSTANCE.convertListInfoToBO(subjectInfoList);

        pageResult.setRecords(subjectInfoBOS);
        pageResult.setTotal(count);
        return pageResult;
    }

    /*
    获取题目的详细信息
     */
    @Override
    public SubjectInfoBO querySubjectInfo(SubjectInfoBO subjectInfoBO) {
        // 通过id查询到 题目主表里面的信息
        SubjectInfo subjectInfo = subjectInfoService.queryById(subjectInfoBO.getId());
        if (Objects.isNull(subjectInfo) ) {
            return null; // 没有值
        }

        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfo.getSubjectType()); // 策略模式
        SubjectOptionBO optionBO = handler.query(subjectInfo.getId().intValue());
        // entity 转 BO
        SubjectInfoBO bo = SubjectInfoConverter.INSTANCE.convertOptionAndInfoToBo(optionBO, subjectInfo);

        // 设置标签,从subject_mapping里面查询到标签
        // 设置查询条件
        SubjectMapping subjectMapping = new SubjectMapping();
        subjectMapping.setSubjectId(subjectInfo.getId());
        subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        // 查询mapping
        List<SubjectMapping> subjectMappings = subjectMappingService.queryLabelId(subjectMapping);
        // 从mappinglist里面得到id的list
        List<Long> labelIds = subjectMappings.stream().map(SubjectMapping::getLabelId).collect(Collectors.toList());
        // 批量查询
        List<SubjectLabel> labels = subjectLabelService.batchQueryById(labelIds);
        List<String> labelNames = labels.stream().map(SubjectLabel::getLabelName).collect(Collectors.toList());

        bo.setLabelName(labelNames);
        return bo;
    }

    @Override
    public PageResult<SubjectInfoEs> getSubjectPageBySearch(SubjectInfoBO subjectInfoBO) {
        SubjectInfoEs subjectInfoEs = new SubjectInfoEs();
        subjectInfoEs.setPageNo(subjectInfoBO.getPageNo());
        subjectInfoEs.setPageSize(subjectInfoBO.getPageSize());
        subjectInfoEs.setKeyWord(subjectInfoBO.getKeyWord()); // 构建es查询的条件（关键词查询,高亮显示）
        return esSubjectService.querySubjectList(subjectInfoEs);
    }
}
