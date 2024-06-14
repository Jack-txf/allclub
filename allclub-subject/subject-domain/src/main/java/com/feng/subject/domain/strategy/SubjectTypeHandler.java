package com.feng.subject.domain.strategy;

import com.feng.subject.common.enums.SubjectInfoTypeEnum;
import com.feng.subject.domain.entity.SubjectInfoBO;

public interface SubjectTypeHandler {

    /**
     * 枚举身份的识别
     */
    SubjectInfoTypeEnum getHandlerType();

    /**
     * 实际的题目的插入
     */
    void add(SubjectInfoBO subjectInfoBO);

}