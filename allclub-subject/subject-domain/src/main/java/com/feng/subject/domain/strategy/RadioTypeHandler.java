package com.feng.subject.domain.strategy;

import com.feng.subject.common.enums.IsDeletedFlagEnum;
import com.feng.subject.common.enums.SubjectInfoTypeEnum;
import com.feng.subject.domain.convert.RadioSubjectConverter;
import com.feng.subject.domain.entity.SubjectAnswerBO;
import com.feng.subject.domain.entity.SubjectInfoBO;
import com.feng.subject.domain.entity.SubjectOptionBO;
import com.feng.subject.infra.basic.entity.SubjectRadio;
import com.feng.subject.infra.basic.service.SubjectRadioService;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 单选题目的策略类
 *
 * @author: txf
 * @date: 2023/10/5
 */
@Component
public class RadioTypeHandler implements SubjectTypeHandler {
    @Resource
    private SubjectRadioService subjectRadioService;

    @Override
    public SubjectInfoTypeEnum getHandlerType() {
        return SubjectInfoTypeEnum.RADIO;
    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        //单选题目的插入
        List<SubjectRadio> subjectRadioList = new LinkedList<>();
        if (subjectInfoBO.getOptionList().isEmpty()) {
            return ; // 是空的！不允许的
        }
        System.out.println("单选题插入：" + subjectInfoBO);

        subjectInfoBO.getOptionList().forEach(option -> {
            SubjectRadio subjectRadio = RadioSubjectConverter.INSTANCE.convertBoToEntity(option);
            subjectRadio.setSubjectId(subjectInfoBO.getId());
            subjectRadio.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
            subjectRadioList.add(subjectRadio);
        });
        subjectRadioService.batchInsert(subjectRadioList);
    }

    @Override
    public SubjectOptionBO query(int subjectId) {
        SubjectRadio subjectRadio = new SubjectRadio();
        subjectRadio.setSubjectId((long) subjectId);
        // 得到单选题的信息
        List<SubjectRadio> result = subjectRadioService.queryByCondition(subjectRadio);
        List<SubjectAnswerBO> subjectAnswerBOList = RadioSubjectConverter.INSTANCE.convertEntityToBoList(result);
        SubjectOptionBO subjectOptionBO = new SubjectOptionBO();
        subjectOptionBO.setOptionList(subjectAnswerBOList);
        return subjectOptionBO;
    }
}
