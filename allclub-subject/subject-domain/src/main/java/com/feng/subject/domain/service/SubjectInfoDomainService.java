package com.feng.subject.domain.service;

import com.feng.subject.common.eneity.PageResult;
import com.feng.subject.domain.entity.SubjectInfoBO;

/**
 * 题目领域服务
 * @author: 田小锋
 * @date: 2023/10/3
 */
public interface SubjectInfoDomainService {

    /**
     * 新增题目
     */
    void add(SubjectInfoBO subjectInfoBO);

    PageResult<SubjectInfoBO> getSubjectPage(SubjectInfoBO subjectInfoBO);

    SubjectInfoBO querySubjectInfo(SubjectInfoBO subjectInfoBO);
}

