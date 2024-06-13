package com.feng.subject.domain.convert;


import com.feng.subject.domain.entity.SubjectLabelBO;
import com.feng.subject.infra.basic.entity.SubjectLabel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper
public interface SubjectLabelConverter {

    SubjectLabelConverter INSTANCE = Mappers.getMapper(SubjectLabelConverter.class);

    SubjectLabel convertBoToLabel(SubjectLabelBO subjectLabelBO);

    List<SubjectLabelBO> convertLabelToBoList(List<SubjectLabel> subjectLabelList);
}
