package com.feng.auth.api.feign;

import com.feng.auth.api.entity.AuthUserDTO;
import com.feng.auth.api.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 用户对外提供接口，服务feign
 *
 * @author: txf
 * @date: 2023/12/3
 */
@FeignClient("all-club-auth")
public interface UserFeignService {

    @RequestMapping("/auth/user/getUserInfo")
    Result<AuthUserDTO> getUserInfo(@RequestBody AuthUserDTO authUserDTO);

    @RequestMapping("/auth/user/listByIds")
    Result<List<AuthUserDTO>> listUserInfoByIds(@RequestBody List<String> userNameList);

}
