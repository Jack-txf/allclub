package com.feng.tim.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feng.tim.im.api.pojo.ChatSession;
import com.feng.tim.im.mapper.ChatSessionMapper;
import com.feng.tim.im.service.ChatSessionService;
import org.springframework.stereotype.Service;

/**
 * 会话表(ChatSession)表服务实现类
 *
 * @author makejava
 * @since 2025-10-12 13:21:08
 */
@Service("chatSessionService")
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

}

