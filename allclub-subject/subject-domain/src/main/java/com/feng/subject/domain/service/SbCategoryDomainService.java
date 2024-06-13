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

    /*
    更新分类
     */
    Boolean update(SubjectCategoryBO subjectCategoryBO);

    /*
    删除分类
     */
    Boolean delete(SubjectCategoryBO subjectCategoryBO);
}
