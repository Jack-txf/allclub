package com.feng.tim.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feng.tim.im.api.pojo.SessionUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话-用户关联表(SessionUser)表数据库访问层
 *
 * @author makejava
 * @since 2025-10-12 13:21:09
 */
@Mapper
public interface SessionUserMapper extends BaseMapper<SessionUser> {
}

