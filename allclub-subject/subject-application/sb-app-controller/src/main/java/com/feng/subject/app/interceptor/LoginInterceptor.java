package com.feng.subject.app.interceptor;

import com.feng.subject.common.context.UserContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 *
 * @author: 田小锋
 * @date: 2023/11/26
 */
public class LoginInterceptor implements HandlerInterceptor {

    // 进入controller之前
    /*
    如果请求头里面有loginId，那么就讲此放进线程本地变量里面，线程隔离
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String loginId = request.getHeader("loginId");
        if (StringUtils.isNotBlank(loginId)) {
            UserContextHolder.set("loginId", loginId);
        }
        return true;
    }

    // 完成controller里面的代码之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserContextHolder.remove();
    }

}
