package com.feng.interview.dao;

import com.feng.interview.entity.po.SubjectCategory;
import com.feng.interview.entity.po.SubjectInfo;
import com.feng.interview.entity.po.SubjectLabel;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface SubjectDao {

    List<SubjectLabel> listAllLabel();

    List<SubjectCategory> listAllCategory();

    List<SubjectInfo> listSubjectByLabelIds(@Param("ids") List<Long> ids);
}

