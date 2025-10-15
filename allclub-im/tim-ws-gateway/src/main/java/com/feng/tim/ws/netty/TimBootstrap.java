package com.feng.tim.ws.netty;

import com.feng.tim.ws.config.WsGatewayConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: 启动器
 * @Author: txf
 * @Date: 2025/10/11
 */
@Component
@Slf4j
public class TimBootstrap {

    @Resource
    private TimServer timServer;

    public void start() {
       timServer.start();
    }

}
