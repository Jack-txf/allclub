package com.feng.practice.server.controller;

import com.alibaba.fastjson.JSON;
import com.feng.practice.api.common.PageResult;
import com.feng.practice.api.common.Result;
import com.feng.practice.api.req.*;
import com.feng.practice.api.vo.*;
import com.feng.practice.server.entity.dto.PracticeSetDTO;
import com.feng.practice.server.entity.dto.PracticeSubjectDTO;
import com.feng.practice.server.service.PracticeSetService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 练习套卷controller
 *
 * @author: 田小锋
 * @date: 2024/3/7
 */
@RestController
@RequestMapping("/practice/set")
@Slf4j
public class PracticeSetController {

    @Resource
    private PracticeSetService practiceSetService;
    //======================================
    // 获取专项练习的内容(说白点就是获取分类及其子类)，获取之后用户选择标签打钩，然后系统根据这些标签生成20道题，给用户来做 TODO 有bug待完善
    //=====================================
    @GetMapping("/getSpecialPracticeContent")
    public Result<List<SpecialPracticeVO>> getSpecialPracticeContent() {
        try {
            List<SpecialPracticeVO> result = practiceSetService.getSpecialPracticeContent();
            if (log.isInfoEnabled()) {
                log.info("getSpecialPracticeContent.result:{}", JSON.toJSONString(result));
            }
            return Result.ok(result);
        } catch (Exception e) {
            log.error("getSpecialPracticeContent.error:{}", e.getMessage(), e);
            return Result.fail("获取专项练习内容失败");
        }

    }

    /*
     * 开始练习 （点击开始练习，系统组装20道题目给用户练习）,故生成的这个套题需要插入数据库中
     */
    @PostMapping(value = "/addPractice")
    public Result<PracticeSetVO> addPractice(@RequestBody GetPracticeSubjectListReq req) {
        if (log.isInfoEnabled()) {
            log.info("获取练习题入参{}", JSON.toJSONString(req));
        }
        try {
            //参数校验
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            Preconditions.checkArgument(!CollectionUtils.isEmpty(req.getAssembleIds()), "标签ids不能为空！");
            PracticeSubjectDTO dto = new PracticeSubjectDTO();
            dto.setAssembleIds(req.getAssembleIds());
            PracticeSetVO practiceSetVO = practiceSetService.addPractice(dto);
            if (log.isInfoEnabled()) {
                log.info("获取练习题目列表出参{}", JSON.toJSONString(practiceSetVO));
            }
            return Result.ok(practiceSetVO);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取练习题目列表异常！错误原因{}", e.getMessage(), e);
            return Result.fail("获取练习题目列表异常！");
        }
    }

    /*
     * 获取这个套卷(上面接口生成的)的所有练习题
     */
    @PostMapping(value = "/getSubjects")
    public Result<PracticeSubjectListVO> getSubjects(@RequestBody GetPracticeSubjectsReq req) {
        if (log.isInfoEnabled()) {
            log.info("获取练习题入参{}", JSON.toJSONString(req));
        }
        try {
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getSetId()), "练习id不能为空！");
            PracticeSubjectListVO list = practiceSetService.getSubjects(req);
            if (log.isInfoEnabled()) {
                log.info("获取练习题目列表出参{}", JSON.toJSONString(list));
            }
            return Result.ok(list);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取练习题目列表异常！错误原因{}", e.getMessage(), e);
            return Result.fail("获取练习题目列表异常！");
        }
    }

    /*
     * 获取每一道题的题目详情（传入参数为题目id，题目类型）
     */
    @PostMapping(value = "/getPracticeSubject")
    public Result<PracticeSubjectVO> getPracticeSubject(@RequestBody GetPracticeSubjectReq req) {
        if (log.isInfoEnabled()) {
            log.info("获取练习题详情入参{}", JSON.toJSONString(req));
        }
        try {
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getSubjectId()), "题目id不能为空！");
            Preconditions.checkArgument(!Objects.isNull(req.getSubjectType()), "题目类型不能为空！");
            PracticeSubjectDTO dto = new PracticeSubjectDTO();
            dto.setSubjectId(req.getSubjectId());
            dto.setSubjectType(req.getSubjectType());
            PracticeSubjectVO vo = practiceSetService.getPracticeSubject(dto);
            if (log.isInfoEnabled()) {
                log.info("获取练习题目详情出参{}", JSON.toJSONString(vo));
            }
            return Result.ok(vo);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取练习详情异常！错误原因{}", e.getMessage(), e);
            return Result.fail("获取练习题目详情异常！");
        }
    }

    /**
     * 获取模拟套题内容
     */
    @PostMapping(value = "/getPreSetContent")
    public Result<PageResult<PracticeSetVO>> getPreSetContent(@RequestBody GetPreSetReq req) {
        if (log.isInfoEnabled()) {
            log.info("获取模拟套题内容入参{}", JSON.toJSONString(req));
        }
        try {
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            PracticeSetDTO dto = new PracticeSetDTO();
            dto.setOrderType(req.getOrderType());
            dto.setPageInfo(req.getPageInfo());
            dto.setSetName(req.getSetName());
            PageResult<PracticeSetVO> list = practiceSetService.getPreSetContent(dto);
            if (log.isInfoEnabled()) {
                log.info("获取模拟套题内容出参{}", JSON.toJSONString(list));
            }
            return Result.ok(list);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取模拟套题内容异常！错误原因{}", e.getMessage(), e);
            return Result.fail("获取模拟套题内容异常！");
        }
    }

    /**
     * 获取未完成的练题内容
     */
    @PostMapping(value = "/getUnCompletePractice")
    public Result<PageResult<UnCompletePracticeSetVO>> getUnCompletePractice(@RequestBody GetUnCompletePracticeReq req) {
        try {
            Preconditions.checkArgument(!Objects.isNull(req), "参数不能为空！");
            PageResult<UnCompletePracticeSetVO> list = practiceSetService.getUnCompletePractice(req);
            if (log.isInfoEnabled()) {
                log.info("获取未完成练习内容出参{}", JSON.toJSONString(list));
            }
            return Result.ok(list);
        } catch (IllegalArgumentException e) {
            log.error("参数异常！错误原因{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取未完成练习内容异常！错误原因{}", e.getMessage(), e);
            return Result.fail("获取未完成练习内容异常！");
        }
    }

}
