package com.feng.subject.app.convert;

import com.feng.subject.app.dto.SubjectInfoDTO;
import com.feng.subject.domain.entity.SubjectInfoBO;
import org.mapstruct.factory.Mappers;

public interface SubjectInfoDTOConverter {
    SubjectInfoDTOConverter INSTANCE = Mappers.getMapper(SubjectInfoDTOConverter.class);

    SubjectInfoBO convertDTOToBO(SubjectInfoDTO subjectInfoDTO);
}
