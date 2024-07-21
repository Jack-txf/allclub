package com.feng.subject.app.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "first-topic", consumerGroup = "test-consumer")
@Slf4j
public class TestConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String s) {
        log.info("接收到mq的消息：{}", s );
    }
}
