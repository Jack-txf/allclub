package com.feng.tim.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feng.tim.im.api.pojo.Message;
import com.feng.tim.im.mapper.MessageMapper;
import com.feng.tim.im.service.MessageService;
import org.springframework.stereotype.Service;

/**
 * 消息表(Message)表服务实现类
 *
 * @author makejava
 * @since 2025-10-12 13:21:09
 */
@Service("messageService")
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

}

