package com.feng.subject.infra.basic.esservice;

import com.feng.subject.common.eneity.PageResult;
import com.feng.subject.infra.basic.espojo.SubjectInfoEs;

public interface EsSubjectService {
    boolean insert(SubjectInfoEs subjectInfoEs);

    PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs subjectInfoEs);
}
