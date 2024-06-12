package com.feng.subject.domain.service;

import com.feng.subject.domain.entity.SubjectCategoryBO;

import java.util.List;

public interface SbCategoryDomainService {
    void add(SubjectCategoryBO subjectCategoryBO);

    /*
    查询大类
     */
    List<SubjectCategoryBO> queryPrimaryCategory();

    List<SubjectCategoryBO> queryCategory(SubjectCategoryBO subjectCategoryBO);
}
