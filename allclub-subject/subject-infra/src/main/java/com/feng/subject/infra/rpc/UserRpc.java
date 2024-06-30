package com.feng.subject.infra.rpc;

import com.feng.auth.api.entity.AuthUserDTO;
import com.feng.auth.api.entity.Result;
import com.feng.auth.api.feign.UserFeignService;
import com.feng.subject.infra.rpc.entity.UserInfo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Williams_Tian
 * @CreateDate 2024/6/28
 * 远程调用auth模块的接口
 */
@Component
public class UserRpc {
    @Resource
    private UserFeignService userFeignService;

    public UserInfo getUserInfo(String userName) {
        AuthUserDTO authUserDTO = new AuthUserDTO();
        authUserDTO.setUserName(userName); // username对应的是数据库中的usernmae，实际是用户微信的openID
        Result<AuthUserDTO> result = userFeignService.getUserInfo(authUserDTO); //远程调用auth服务
        UserInfo userInfo = new UserInfo();
        if (!result.getSuccess()) {
            return userInfo;
        }
        AuthUserDTO data = result.getData();
        userInfo.setUserName(data.getUserName());
        userInfo.setNickName(data.getNickName());
        userInfo.setAvatar(data.getAvatar());
        return userInfo;
    }

}
