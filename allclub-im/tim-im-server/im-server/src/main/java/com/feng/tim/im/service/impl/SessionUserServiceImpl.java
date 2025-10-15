package com.feng.tim.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feng.tim.im.mapper.SessionUserMapper;
import com.feng.tim.im.api.pojo.SessionUser;
import com.feng.tim.im.service.SessionUserService;
import org.springframework.stereotype.Service;

/**
 * 会话-用户关联表(SessionUser)表服务实现类
 *
 * @author makejava
 * @since 2025-10-12 13:21:09
 */
@Service("sessionUserService")
public class SessionUserServiceImpl extends ServiceImpl<SessionUserMapper, SessionUser> implements SessionUserService {

}

