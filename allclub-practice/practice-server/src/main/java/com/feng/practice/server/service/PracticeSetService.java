package com.feng.practice.server.service;

import com.feng.practice.api.req.GetPracticeSubjectsReq;
import com.feng.practice.api.vo.PracticeSetVO;
import com.feng.practice.api.vo.PracticeSubjectListVO;
import com.feng.practice.api.vo.PracticeSubjectVO;
import com.feng.practice.api.vo.SpecialPracticeVO;
import com.feng.practice.server.entity.dto.PracticeSubjectDTO;

import java.util.List;

public interface PracticeSetService {
    // 1.获取专项练习的内容(获取专项--也就是一二级分类，标签)
    List<SpecialPracticeVO> getSpecialPracticeContent();

    // 2.开始练习 -- 选择好标签之后点击按钮，生成一个套题
    PracticeSetVO addPractice(PracticeSubjectDTO dto);

    /*
     * 3.获取上面生成套题的练习题的集合
     */
    PracticeSubjectListVO getSubjects(GetPracticeSubjectsReq req);

    /*
     * 4.获取套题每个题目的题目详情
     */
    PracticeSubjectVO getPracticeSubject(PracticeSubjectDTO dto);
}
