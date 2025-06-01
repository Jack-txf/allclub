package com.feng.im.init;


import com.feng.im.netty.NettyWebsocketServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


/**
 * @version 1.0
 * @Author txf
 * @Date 2025/4/2 16:36
 * @注释 Netty启动器
 */
@Component
public class NettyStarter{ // 这里实现InitializingBean接口，是为了分析springboot启动流程的，此处不必理会
    @Value("${websocket.port}")
    private int port;

    @Resource
    private NettyWebsocketServer server;

    @PostConstruct
    public void init() {

        if ( port != 0 ) server.setPort(port);
        // 新开一个线程启动Netty服务
        //NettyServer.start();
        new Thread(server::start).start();
    }
}
