package com.feng.tim.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feng.tim.im.api.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息表(Message)表数据库访问层
 *
 * @author makejava
 * @since 2025-10-12 13:21:09
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {


}

