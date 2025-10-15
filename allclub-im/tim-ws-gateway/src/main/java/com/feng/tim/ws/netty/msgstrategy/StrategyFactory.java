package com.feng.tim.ws.netty.msgstrategy;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 策略工厂类
 * @Author: txf
 * @Date: 2025/10/12
 */
@Component
@Slf4j
public class StrategyFactory {
    @Autowired
    private List<MessageStrategy> messageStrategies;

    private final Map<String, MessageStrategy> factories;

    public StrategyFactory() {
        factories = new HashMap<>(8);
    }

    @PostConstruct
    public void init() {
        messageStrategies.forEach(strategy ->
                factories.put( strategy.getType(),  strategy));
    }

    public MessageStrategy getStrategy(String type) {
        return factories.get(type);
    }

}
