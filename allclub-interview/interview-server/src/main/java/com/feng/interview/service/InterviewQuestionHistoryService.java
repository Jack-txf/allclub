package com.feng.interview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.feng.interview.api.vo.InterviewQuestionHistoryVO;
import com.feng.interview.entity.po.InterviewQuestionHistory;

import java.util.List;

/**
 * 面试题目记录表(InterviewQuestionHistory)表服务接口
 *
 * @author makejava
 */
public interface InterviewQuestionHistoryService extends IService<InterviewQuestionHistory> {

    List<InterviewQuestionHistoryVO> detail(Long id);

}
