package com.feng.interview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.feng.interview.api.common.PageResult;
import com.feng.interview.api.req.InterviewHistoryReq;
import com.feng.interview.api.req.InterviewSubmitReq;
import com.feng.interview.api.vo.InterviewHistoryVO;
import com.feng.interview.api.vo.InterviewResultVO;
import com.feng.interview.entity.po.InterviewHistory;

/**
 * 面试汇总记录表(InterviewHistory)表服务接口
 *
 * @author makejava
 * @since 2024-05-23 22:56:03
 */
public interface InterviewHistoryService extends IService<InterviewHistory> {

    void logInterview(InterviewSubmitReq req, InterviewResultVO submit);


    PageResult<InterviewHistoryVO> getHistory(InterviewHistoryReq req);

}
