package com.feng.auth.domain.service.impl;

import com.feng.auth.domain.redis.RedisUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.feng.auth.common.enums.IsDeletedFlagEnum;
import com.feng.auth.domain.convert.AuthPermissionBOConverter;
import com.feng.auth.domain.entity.AuthPermissionBO;
import com.feng.auth.domain.service.AuthPermissionDomainService;
import com.feng.auth.infra.basic.entity.AuthPermission;
import com.feng.auth.infra.basic.service.AuthPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthPermissionDomainServiceImpl implements AuthPermissionDomainService {
    @Resource
    private AuthPermissionService authPermissionService;
    @Resource
    private RedisUtil redisUtil;
    private final String authPermissionPrefix = "auth.permission";

    @Override
    public Boolean add(AuthPermissionBO authPermissionBO) {
        AuthPermission authPermission = AuthPermissionBOConverter.INSTANCE.convertBOToEntity(authPermissionBO);
        authPermission.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        int count = authPermissionService.insert(authPermission);
        return count > 0;
    }

    @Override
    public Boolean update(AuthPermissionBO authPermissionBO) {
        AuthPermission authPermission = AuthPermissionBOConverter.INSTANCE.convertBOToEntity(authPermissionBO);
        int count = authPermissionService.update(authPermission);
        return count > 0;
    }

    @Override
    public Boolean delete(AuthPermissionBO authPermissionBO) {
        AuthPermission authPermission = new AuthPermission();
        authPermission.setId(authPermissionBO.getId());
        authPermission.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        int count = authPermissionService.update(authPermission);
        return count > 0;
    }

    /*
    根据用户userName拿到该用户的所有权限
     */
    @Override
    public List<String> getPermission(String userName) {
        String permissionKey = redisUtil.buildKey(authPermissionPrefix, userName);
        String permissionValue = redisUtil.get(permissionKey);
        if (StringUtils.isBlank(permissionValue)) {
            return Collections.emptyList();
        }
        List<AuthPermission> permissionList = new Gson().fromJson(permissionValue,
                new TypeToken<List<AuthPermission>>() {
                }.getType());
        return permissionList.stream().map(AuthPermission::getPermissionKey).collect(Collectors.toList());
    }

}
