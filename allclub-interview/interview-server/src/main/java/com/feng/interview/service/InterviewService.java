package com.feng.interview.service;


import com.feng.interview.api.req.InterviewReq;
import com.feng.interview.api.req.InterviewSubmitReq;
import com.feng.interview.api.req.StartReq;
import com.feng.interview.api.vo.InterviewQuestionVO;
import com.feng.interview.api.vo.InterviewResultVO;
import com.feng.interview.api.vo.InterviewVO;

public interface InterviewService {

    InterviewVO analyse(InterviewReq req);

    InterviewQuestionVO start(StartReq req);

    InterviewResultVO submit(InterviewSubmitReq req);
}
