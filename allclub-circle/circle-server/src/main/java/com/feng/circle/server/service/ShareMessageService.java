package com.feng.circle.server.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.feng.circle.api.common.PageResult;
import com.feng.circle.api.req.GetShareMessageReq;
import com.feng.circle.api.vo.ShareMessageVO;
import com.feng.circle.server.entity.po.ShareMessage;

/**
 * <p>
 * 消息表 服务类
 * </p>
 *
 * @author txf
 * @since 2024/05/18
 */
public interface ShareMessageService extends IService<ShareMessage> {

    PageResult<ShareMessageVO> getMessages(GetShareMessageReq req);

    void comment(String fromId, String toId, Long targetId);

    void reply(String fromId, String toId, Long targetId);

    Boolean unRead();

}
