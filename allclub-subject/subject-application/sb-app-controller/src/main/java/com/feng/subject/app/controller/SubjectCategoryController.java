package com.feng.subject.app.controller;

import com.alibaba.fastjson.JSON;
import com.feng.subject.app.convert.SubjectCategoryDTOConverter;
import com.feng.subject.app.dto.SubjectCategoryDTO;
import com.feng.subject.common.eneity.Result;
import com.feng.subject.domain.entity.SubjectCategoryBO;
import com.feng.subject.domain.service.SbCategoryDomainService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/subject/category")
@Slf4j
public class SubjectCategoryController {
    @Resource
    private SbCategoryDomainService sbCategoryDomainService;

    // 新增分类
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody SubjectCategoryDTO subjectCategoryDTO){
        try{
            if ( log.isInfoEnabled() ) {
                log.info("SubjectCategoryController.add.dto:{}", JSON.toJSONString(subjectCategoryDTO));
            }
            Preconditions.checkNotNull(subjectCategoryDTO.getCategoryType(), "分类类型不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(subjectCategoryDTO.getCategoryName()), "分类名称不能为空");
            Preconditions.checkNotNull(subjectCategoryDTO.getParentId(), "分类父级id不能为空");

            SubjectCategoryBO subjectCategoryBO = SubjectCategoryDTOConverter.INSTANCE
                    .convertDtoToCategoryBO(subjectCategoryDTO);
            sbCategoryDomainService.add(subjectCategoryBO);
            return Result.ok(true);
        }catch (Exception e) {
            return Result.fail("添加分类失败");
        }
    }

    /*
    查询大类
     */
    @GetMapping("/queryPrimaryCategory")
    public Result<List<SubjectCategoryDTO>> queryPrimaryCategory() {
        try {
            List<SubjectCategoryBO> subjectCategoryBOList =
                    sbCategoryDomainService.queryPrimaryCategory();
            List<SubjectCategoryDTO> categoryDTOS = SubjectCategoryDTOConverter.INSTANCE
                    .convertBoToCategoryDTOList(subjectCategoryBOList);
            return Result.ok(categoryDTOS);
        }catch (Exception e) {
            log.error("error：{}", e.getMessage());
            return Result.fail("查询大类失败");
        }
    }

    /*
    查询二级分类，通过一级分类
     */
    @PostMapping("/queryCategoryByPrimary")
    public Result<List<SubjectCategoryDTO>> queryCategoryByPrimary(@RequestBody SubjectCategoryDTO subjectCategoryDTO) {
        try {
            // 打印日志,查看参数传递是否有误
            if ( log.isInfoEnabled() ) {
                log.info("SubjectCategoryController.add.dto:{}", JSON.toJSONString(subjectCategoryDTO));
            }
            Preconditions.checkNotNull(subjectCategoryDTO.getParentId(), "分类父级ID不能是空的!");
            // DTO转BO
            SubjectCategoryBO subjectCategoryBO = SubjectCategoryDTOConverter.INSTANCE
                    .convertDtoToCategoryBO(subjectCategoryDTO);
            // 把BO传进去查询
            List<SubjectCategoryBO> subjectCategoryBOList =
                    sbCategoryDomainService.queryCategory(subjectCategoryBO);
            // 查询出来的BOlist转为DTOlist
            List<SubjectCategoryDTO> categoryDTOS = SubjectCategoryDTOConverter.INSTANCE
                    .convertBoToCategoryDTOList(subjectCategoryBOList);
            // 返回结果
            return Result.ok(categoryDTOS);
        }catch (Exception e) {
            log.error("error：{}", e.getMessage());
            return Result.fail("查询二级分类失败");
        }
    }

    /*
    更新分类
     */
    @PostMapping("/upodate")
    public Result<Boolean> update(@RequestBody SubjectCategoryDTO subjectCategoryDTO) {
        try{
            if ( log.isInfoEnabled() ) {
                log.info("SubjectCategoryController.add.dto:{}", JSON.toJSONString(subjectCategoryDTO));
            }
            // DTO转BO
            SubjectCategoryBO subjectCategoryBO = SubjectCategoryDTOConverter.INSTANCE
                    .convertDtoToCategoryBO(subjectCategoryDTO);
            Boolean res = sbCategoryDomainService.update(subjectCategoryBO);
            return Result.ok(res);
        }catch (Exception e) {
            log.error("error:{}", e.getMessage());
            return Result.fail("更新分类失败!");
        }
    }

    /*
    删除分类
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody SubjectCategoryDTO subjectCategoryDTO) {
        try{
            if ( log.isInfoEnabled() ) {
                log.info("SubjectCategoryController.delete.dto:{}", JSON.toJSONString(subjectCategoryDTO));
            }
            // DTO转BO
            SubjectCategoryBO subjectCategoryBO = SubjectCategoryDTOConverter.INSTANCE
                    .convertDtoToCategoryBO(subjectCategoryDTO);
            Boolean res = sbCategoryDomainService.delete(subjectCategoryBO);
            return Result.ok(res);
        }catch (Exception e) {
            log.error("error:{}", e.getMessage());
            return Result.fail("删除分类失败!");
        }
    }


}
