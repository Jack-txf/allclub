package com.feng.subject.domain.strategy;


import com.feng.subject.common.enums.IsDeletedFlagEnum;
import com.feng.subject.common.enums.SubjectInfoTypeEnum;
import com.feng.subject.domain.convert.BriefSubjectConverter;
import com.feng.subject.domain.entity.SubjectInfoBO;
import com.feng.subject.infra.basic.entity.SubjectBrief;
import com.feng.subject.infra.basic.service.SubjectBriefService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 简答题目的策略类
 * 
 * @author: txf
 * @date: 2023/10/5
 */
@Component
public class BriefTypeHandler implements SubjectTypeHandler{

    @Resource
    private SubjectBriefService subjectBriefService;

    @Override
    public SubjectInfoTypeEnum getHandlerType() {
        return SubjectInfoTypeEnum.BRIEF;
    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        SubjectBrief subjectBrief = BriefSubjectConverter.INSTANCE.convertBoToEntity(subjectInfoBO);
        subjectBrief.setSubjectId(subjectInfoBO.getId().intValue());
        subjectBrief.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectBriefService.insert(subjectBrief);
    }

}
