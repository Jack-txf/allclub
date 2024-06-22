package com.feng.auth.domain.service.impl;

import com.feng.auth.common.enums.IsDeletedFlagEnum;
import com.feng.auth.domain.entity.AuthRolePermissionBO;
import com.feng.auth.domain.service.AuthRolePermissionDomainService;
import com.feng.auth.infra.basic.entity.AuthRolePermission;
import com.feng.auth.infra.basic.service.AuthRolePermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class AuthRolePermissionDomainServiceImpl implements AuthRolePermissionDomainService {

    @Resource
    private AuthRolePermissionService authRolePermissionService;

    @Override
    public Boolean add(AuthRolePermissionBO authRolePermissionBO) {
        List<AuthRolePermission> rolePermissionList = new LinkedList<>();
        Long roleId = authRolePermissionBO.getRoleId(); // 获取角色ID
        /*
        一个roleId与多个permissionId对应起来
         */
        authRolePermissionBO.getPermissionIdList().forEach(permissionId -> {
            AuthRolePermission authRolePermission = new AuthRolePermission();
            authRolePermission.setRoleId(roleId);
            authRolePermission.setPermissionId(permissionId);
            authRolePermission.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode()); // 设置为未删除
            rolePermissionList.add(authRolePermission); // (roleId, permissionId)映射插入数据库表中
        });
        int count = authRolePermissionService.batchInsert(rolePermissionList);
        return count > 0;
    }


}
