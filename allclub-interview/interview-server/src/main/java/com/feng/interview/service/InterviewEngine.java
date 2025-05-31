package com.feng.interview.service;

import com.feng.interview.api.enums.EngineEnum;
import com.feng.interview.api.req.InterviewSubmitReq;
import com.feng.interview.api.req.StartReq;
import com.feng.interview.api.vo.InterviewQuestionVO;
import com.feng.interview.api.vo.InterviewResultVO;
import com.feng.interview.api.vo.InterviewVO;

import java.util.List;

/**
 * 面试引擎
 *
 * @author txf
 */
public interface InterviewEngine {

    /**
     * 引擎类型
     */
    EngineEnum engineType();

    /**
     * 通过简历关键字获取面试关键字
     */
    InterviewVO analyse(List<String> KeyWords);

    /**
     * 通过面试关键字获取面试题
     */
    InterviewQuestionVO start(StartReq req);

    /**
     * 提交面试题
     */
    InterviewResultVO submit(InterviewSubmitReq req);

}
