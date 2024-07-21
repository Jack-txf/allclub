package com.feng.circle.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.feng.circle.api.common.PageResult;
import com.feng.circle.api.req.GetShareMomentReq;
import com.feng.circle.api.req.RemoveShareMomentReq;
import com.feng.circle.api.req.SaveMomentCircleReq;
import com.feng.circle.api.vo.ShareMomentVO;
import com.feng.circle.server.entity.po.ShareMoment;

/**
 * <p>
 * 动态信息 服务类
 * </p>
 *
 * @author txf
 * @since 2024/05/16
 */
public interface ShareMomentService extends IService<ShareMoment> {

    Boolean saveMoment(SaveMomentCircleReq req);

    PageResult<ShareMomentVO> getMoments(GetShareMomentReq req);

    Boolean removeMoment(RemoveShareMomentReq req);

    void incrReplyCount(Long id, int count);

}
