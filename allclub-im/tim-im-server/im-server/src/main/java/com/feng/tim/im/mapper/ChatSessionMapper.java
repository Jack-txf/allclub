package com.feng.tim.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feng.tim.im.api.pojo.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话表(ChatSession)表数据库访问层
 *
 * @author makejava
 * @since 2025-10-12 13:21:02
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

}

