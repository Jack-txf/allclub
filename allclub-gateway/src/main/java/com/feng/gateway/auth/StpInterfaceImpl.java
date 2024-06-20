package com.feng.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.feng.gateway.entity.AuthPermission;
import com.feng.gateway.entity.AuthRole;
import com.feng.gateway.redis.RedisUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.data.redis.core.RedisTemplate;
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
 * @date: 2024/3/20
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    @Resource
    private RedisUtil redisUtil;
    private final String authPermissionPrefix = "auth.permission";
    private final String authRolePrefix = "auth.role";

    // 获取用户的权限集合
    /*
    三种方式：1.直接查询数据库 2.权限数据缓存到redis中，查redis
        3.如果redis中没有，就查数据库。 此处选择第二种
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return getAuth(loginId.toString(), authPermissionPrefix);
    }

    // 角色集合
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return getAuth(loginId.toString(), authRolePrefix);
    }

    /*
    redis里面：
        登录完成之后，将该用户的权限，角色存入redis，存储的形式是 key:value；
        但是value是JSON字符串，
     */
    private List<String> getAuth(String loginId, String prefix) {
        String authKey = redisUtil.buildKey(prefix, loginId); // 构建权限认证的key
        String authValue = redisUtil.get(authKey); // 从redis里面获取key对应的值
        if (StringUtils.isBlank(authValue)) {
            return Collections.emptyList(); // 返回空集合
        }
        List<String> authList = new ArrayList<>();
        if (authRolePrefix.equals(prefix)) { // 如果是要xx角色
            List<AuthRole> roleList = new Gson().fromJson(authValue,
                    new TypeToken<List<AuthRole>>() {}.getType());// 把authValueJSON字符串转成角色集合
            authList = roleList.stream().map(AuthRole::getRoleKey).collect(Collectors.toList());
        } else if (authPermissionPrefix.equals(prefix)) { // 如果是校验xx权限
            List<AuthPermission> permissionList = new Gson().fromJson(authValue,
                    new TypeToken<List<AuthPermission>>() {}.getType());
            authList = permissionList.stream().map(AuthPermission::getPermissionKey).collect(Collectors.toList());
        }
        return authList;
    }

}
