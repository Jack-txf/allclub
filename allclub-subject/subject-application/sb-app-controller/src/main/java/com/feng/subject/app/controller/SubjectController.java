package com.feng.subject.app.controller;

import com.feng.subject.infra.basic.entity.SubjectCategory;
import com.feng.subject.infra.basic.service.SubjectCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class SubjectController {
    @Resource
    private SubjectCategoryService subjectCategoryService;

    @GetMapping("/subject/test")
    public String test() {
        return "Hello World\n";
    }

    @GetMapping("/subject/testlist")
    public String test02() {
        SubjectCategory category = subjectCategoryService.queryById(1L);
        return category.getCategoryName();
    }
}
