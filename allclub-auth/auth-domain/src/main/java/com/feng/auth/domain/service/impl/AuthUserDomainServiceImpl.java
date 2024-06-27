package com.feng.auth.domain.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.feng.auth.domain.redis.RedisUtil;
import com.google.gson.Gson;
import com.feng.auth.common.enums.AuthUserStatusEnum;
import com.feng.auth.common.enums.IsDeletedFlagEnum;
import com.feng.auth.domain.constants.AuthConstant;
import com.feng.auth.domain.convert.AuthUserBOConverter;
import com.feng.auth.domain.entity.AuthUserBO;
import com.feng.auth.domain.service.AuthUserDomainService;
import com.feng.auth.infra.basic.entity.*;
import com.feng.auth.infra.basic.service.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthUserDomainServiceImpl implements AuthUserDomainService {

    @Resource
    private AuthUserService authUserService;
    @Resource
    private AuthUserRoleService authUserRoleService;
    @Resource
    private AuthPermissionService authPermissionService;
    @Resource
    private AuthRolePermissionService authRolePermissionService;
    @Resource
    private AuthRoleService authRoleService;
    @Resource
    private RedisUtil redisUtil;
    private static final String LOGIN_PREFIX = "wx-loginCode"; // 微信登录的标记

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    // TODO 待完善
    public Boolean register(AuthUserBO authUserBO) {
        //1. 校验用户是否存在 (微信公众号验证码登录)
        AuthUser existAuthUser = new AuthUser();
        existAuthUser.setUserName(authUserBO.getUserName()); // 这里的userName是存的openID
        List<AuthUser> existUser = authUserService.queryByCondition(existAuthUser);
        if (!existUser.isEmpty()) {
            return true;
        }
        // BO -> entity
        AuthUser user = AuthUserBOConverter.INSTANCE.convertBOToEntity(authUserBO);
        user.setStatus(AuthUserStatusEnum.OPEN.getCode()); //用户设置为启用
        user.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode()); // 设置为未删除
        // 密码加密
        // 加盐
        String salt = "txf_lh";
        user.setPassword(SaSecureUtil.md5BySalt(user.getPassword(), salt));
        // 2.插入user表
        Integer count = authUserService.insert(user);
        // 给注册的用户一个最初始的角色，normal_user普通用户
        AuthRole authRole = new AuthRole();
        authRole.setRoleKey(AuthConstant.NORMAL_USER);
        AuthRole roleResult = authRoleService.queryByCondition(authRole); // 根据roleKey查到该条角色信息
        Long roleId = roleResult.getId(); // 角色ID
        Long userId = user.getId(); // 用户ID
        AuthUserRole authUserRole = new AuthUserRole();
        authUserRole.setUserId(userId);
        authUserRole.setRoleId(roleId);
        authUserRole.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode()); // 组装用户-角色关联的对象
        authUserRoleService.insert(authUserRole); // 3.插入用户角色关联表

        //4.把该用户的角色集合，权限集合查出来，存到redis里面去
        String authRolePrefix = "auth.role";
        String roleKey = redisUtil.buildKey(authRolePrefix, user.getUserName());
        List<AuthRole> roleList = new LinkedList<>();
        roleList.add(authRole); //角色集合
        redisUtil.set(roleKey, new Gson().toJson(roleList)); // 角色集合存入redis

        AuthRolePermission authRolePermission = new AuthRolePermission();
        authRolePermission.setRoleId(roleId);
        List<AuthRolePermission> rolePermissionList = authRolePermissionService.
                queryByCondition(authRolePermission); // 根绝roleId查到所有权限
        List<Long> permissionIdList = rolePermissionList.stream()
                .map(AuthRolePermission::getPermissionId).collect(Collectors.toList()); // 权限的id数组
        // 通过权限的id数组，查到所有权限的对象集合
        List<AuthPermission> permissionList = authPermissionService.queryByRoleList(permissionIdList);
        String authPermissionPrefix = "auth.permission";
        String permissionKey = redisUtil.buildKey(authPermissionPrefix, user.getUserName());
        redisUtil.set(permissionKey, new Gson().toJson(permissionList)); // 权限对象集合存入redis
        return count > 0;
    }

    @Override
    public Boolean update(AuthUserBO authUserBO) {
        AuthUser authUser = AuthUserBOConverter.INSTANCE.convertBOToEntity(authUserBO);
        Integer count = authUserService.updateByUserName(authUser);
        //有任何的更新，都要与缓存进行同步的修改
        return count > 0;
    }

    @Override
    public Boolean delete(AuthUserBO authUserBO) {
        AuthUser authUser = new AuthUser();
        authUser.setId(authUserBO.getId());
        authUser.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        Integer count = authUserService.update(authUser);
        //有任何的更新，都要与缓存进行同步的修改
        return count > 0;
    }

    // TODO 待完善
    @Override
    public SaTokenInfo doLogin(String validCode) {
        // 从Redis中拿到openid
        String key = redisUtil.buildKey(LOGIN_PREFIX + validCode);
        String openID = redisUtil.get(key);
        if (StringUtils.isBlank(openID)) {
            return null;
        }
        AuthUserBO authUserBO = new AuthUserBO();
        authUserBO.setUserName(openID);
        this.register(authUserBO); // 这个会导致事务失效啊。。。。那怎么解决呢？
        StpUtil.login(openID); // 这里是openID，===========sa-token登录是openID作为标识的!!!!
        return StpUtil.getTokenInfo();
    }

    @Override
    public AuthUserBO getUserInfo(AuthUserBO authUserBO) {
        AuthUser authUser = new AuthUser();
        authUser.setUserName(authUserBO.getUserName());
        List<AuthUser> userList = authUserService.queryByCondition(authUser);
        if (CollectionUtils.isEmpty(userList)) {
            return new AuthUserBO();
        }
        AuthUser user = userList.get(0);
        return AuthUserBOConverter.INSTANCE.convertEntityToBO(user);
    }

    @Override
    public List<AuthUserBO> listUserInfoByIds(List<String> userNameList) {
        List<AuthUser> userList = authUserService.listUserInfoByIds(userNameList);
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }
        return AuthUserBOConverter.INSTANCE.convertEntityToBO(userList);
    }

    @Override
    public Object changeStatus(AuthUserBO authUserBO) {
        AuthUser user = AuthUserBOConverter.INSTANCE.convertBOToEntity(authUserBO);
        Integer update = authUserService.update(user);
        return update > 0;
    }
}
