package com.feng.tim.im.service.impl;

import com.feng.tim.im.api.TestDubboIMService;
import com.feng.tim.im.test.AutowiredObj;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/10/11
 */
@Slf4j
@DubboService
public class TestDubboIMServiceImpl implements TestDubboIMService {
    @Resource
    private AutowiredObj autowiredObj;

    @Override
    public String imServer(String params) {
        log.info("imServer服务: {}", params);
        return "imServer: " + params + " --" + autowiredObj.getName();
    }
}
