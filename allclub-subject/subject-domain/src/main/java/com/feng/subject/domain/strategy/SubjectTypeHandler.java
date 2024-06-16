package com.feng.subject.domain.strategy;

import com.feng.subject.common.enums.SubjectInfoTypeEnum;
import com.feng.subject.domain.entity.SubjectInfoBO;
import com.feng.subject.domain.entity.SubjectOptionBO;

public interface SubjectTypeHandler {

    /**
     * 题目类型的识别
     */
    SubjectInfoTypeEnum getHandlerType();

    /**
     * 实际的题目的插入
     */
    void add(SubjectInfoBO subjectInfoBO);
    /**
     * 实际的题目的插入
     */
    SubjectOptionBO query(int subjectId);

}