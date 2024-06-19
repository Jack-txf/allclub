package com.feng.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义权限验证接口扩展
 *
 * @author: txf
 * @date: 2023/10/28
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        ArrayList<String> list = new ArrayList<>();
        list.add("user:add");
        return list;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        ArrayList<String> list = new ArrayList<>();
        list.add("admin");
        return list;
    }

}
