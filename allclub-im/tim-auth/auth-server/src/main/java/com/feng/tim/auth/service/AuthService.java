package com.feng.tim.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.feng.tim.auth.api.pojo.User;
import com.feng.tim.auth.common.R;
import com.feng.tim.auth.controller.dto.LoginForm;
import com.feng.tim.auth.controller.dto.RegisterForm;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/10/9
 */
public interface AuthService extends IService<User> {
    R loginByPwd(LoginForm loginForm);

    R registerByForm(RegisterForm registerForm);

    R genUsername();
}
