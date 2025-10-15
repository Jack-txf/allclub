package com.feng.tim.auth.controller;

import com.feng.tim.auth.common.R;
import com.feng.tim.auth.constant.Token;
import com.feng.tim.auth.controller.dto.LoginForm;
import com.feng.tim.auth.controller.dto.RegisterForm;
import com.feng.tim.auth.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/10/9
 */
@RestController
@RequestMapping("/user/auth")
public class AuthController {
    @Resource
    private AuthService authService;

    @PostMapping("/login")
    public R login(@RequestBody LoginForm loginForm, HttpServletResponse response) {
        // 校验参数
        R r = authService.loginByPwd(loginForm);
        if ( r.getData().get(Token.TOKEN_PREFIX) != null ) {
            response.setHeader(Token.TOKEN_PREFIX, r.getData().get("tim-token").toString());
            return R.success().setData("msg", "登录成功！");
        } else
            return r;
    }

    @PostMapping("/registerByForm")
    public R registerByForm(@RequestBody RegisterForm registerForm ) {
        // 校验参数
        return authService.registerByForm(registerForm);
    }

    @GetMapping("/genUsername")
    public R genUsername() {
        return authService.genUsername();
    }
}
