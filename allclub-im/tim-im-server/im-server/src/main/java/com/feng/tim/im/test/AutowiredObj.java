package com.feng.tim.im.test;

import org.springframework.stereotype.Component;

/**
 * @Description: 测试注入的对象
 * @Author: txf
 * @Date: 2025/10/11
 */
@Component
public class AutowiredObj {
    public String getName() {
        return "RPC-AutowiredObj";
    }
}
