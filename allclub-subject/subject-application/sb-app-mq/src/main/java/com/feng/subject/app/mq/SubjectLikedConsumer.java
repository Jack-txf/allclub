package com.feng.subject.app.mq;

import com.alibaba.fastjson.JSON;
import com.feng.subject.domain.entity.SubjectLikedBO;
import com.feng.subject.domain.service.SubjectLikedDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@RocketMQMessageListener(topic = "subject-liked", consumerGroup = "test-consumer")
@Slf4j
public class SubjectLikedConsumer implements RocketMQListener<String> {
    @Resource
    private SubjectLikedDomainService subjectLikedDomainService;
    @Override
    public void onMessage(String s) {
        log.info("同步点赞数据的消费者，接收到mq的消息：{}", s );
        SubjectLikedBO subjectLikedBO = JSON.parseObject(s, SubjectLikedBO.class);
        subjectLikedDomainService.syncLikedByMsg(subjectLikedBO);
    }
}
