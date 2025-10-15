package com.feng.tim.ws.dubbo;

import com.feng.tim.im.api.TestDubboIMService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * @Description: im业务处理dubbo
 * @Author: txf
 * @Date: 2025/10/11
 */
@Component
public class IMServerTackle {
    @DubboReference
    private TestDubboIMService testDubboIMService;

    public String imServer(String params) {
        return testDubboIMService.imServer(params);
    }
}
