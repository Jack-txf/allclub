package com.feng.subject.app.controller;

import com.alibaba.fastjson.JSON;
import com.feng.subject.app.convert.SubjectAnswerDTOConverter;
import com.feng.subject.app.convert.SubjectInfoDTOConverter;
import com.feng.subject.app.dto.SubjectInfoDTO;
import com.feng.subject.common.eneity.Result;
import com.feng.subject.domain.entity.SubjectAnswerBO;
import com.feng.subject.domain.entity.SubjectInfoBO;
import com.feng.subject.domain.service.SubjectInfoDomainService;
import com.feng.subject.infra.basic.entity.SubjectCategory;
import com.feng.subject.infra.basic.service.SubjectCategoryService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/subject")
@Slf4j
public class SubjectController {
    @Resource
    private SubjectCategoryService subjectCategoryService;
    @Resource
    private SubjectInfoDomainService subjectInfoDomainService;

    @GetMapping("/test")
    public String test() {
        return "Hello World\n";
    }

    @GetMapping("/testlist")
    public String test02() {
        SubjectCategory category = subjectCategoryService.queryById(1L);
        return category.getCategoryName();
    }

    /*
    添加题目
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody SubjectInfoDTO subjectInfoDTO ) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectController.add.dto:{}", JSON.toJSONString(subjectInfoDTO));
            }
            /*
            1.参数校验
             */
            Preconditions.checkArgument(!StringUtils.isBlank(subjectInfoDTO.getSubjectName()),
                    "题目名称不能为空");
            Preconditions.checkNotNull(subjectInfoDTO.getSubjectDifficult(), "题目难度不能为空");
            Preconditions.checkNotNull(subjectInfoDTO.getSubjectType(), "题目类型不能为空");
            Preconditions.checkNotNull(subjectInfoDTO.getSubjectScore(), "题目分数不能为空");
            Preconditions.checkArgument(!CollectionUtils.isEmpty(subjectInfoDTO.getCategoryIds())
                    , "分类id不能为空");
            Preconditions.checkArgument(!CollectionUtils.isEmpty(subjectInfoDTO.getLabelIds())
                    , "标签id不能为空");
            /*
            2.DTO转BO, 由于有嵌套的 List对象，转换多一步
             */
            SubjectInfoBO subjectInfoBO = SubjectInfoDTOConverter.INSTANCE.convertDTOToBO(subjectInfoDTO);
            List<SubjectAnswerBO> answerBOS = SubjectAnswerDTOConverter.INSTANCE
                    .convertListDTOToBO(subjectInfoDTO.getOptionList());
            subjectInfoBO.setOptionList(answerBOS); // 转换完成！
            /*
            3.传递参数给下一级
             */
            subjectInfoDomainService.add(subjectInfoBO);

            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectCategoryController.add.error:{}", e.getMessage(), e);
            return Result.fail("新增题目失败");
        }
    }

}
