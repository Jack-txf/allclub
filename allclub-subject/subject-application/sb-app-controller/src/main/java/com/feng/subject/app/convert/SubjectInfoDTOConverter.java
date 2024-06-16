package com.feng.subject.app.convert;

import com.feng.subject.app.dto.SubjectInfoDTO;
import com.feng.subject.domain.entity.SubjectInfoBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectInfoDTOConverter {
    SubjectInfoDTOConverter INSTANCE = Mappers.getMapper(SubjectInfoDTOConverter.class);
    SubjectInfoBO convertDTOToBO(SubjectInfoDTO subjectInfoDTO);
    SubjectInfoDTO convertBOToDTO(SubjectInfoBO subjectInfoBO);
    List<SubjectInfoDTO> convertBOToDTOList(List<SubjectInfoBO> subjectInfoBO);
}
