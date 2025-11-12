package com.feng.tim.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.feng.tim.auth.api.pojo.User;
import com.feng.tim.auth.common.R;
import com.feng.tim.auth.config.LoginThreadPool;
import com.feng.tim.auth.constant.RedisKey;
import com.feng.tim.auth.constant.Token;
import com.feng.tim.auth.controller.dto.LoginForm;
import com.feng.tim.auth.controller.dto.RegisterForm;
import com.feng.tim.auth.jwt.JwtUtils;
import com.feng.tim.auth.mapper.UserMapper;
import com.feng.tim.auth.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/10/9
 */
@Service
@Slf4j
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService{
    @Resource
    private UserMapper userMapper;
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private ThreadPoolExecutor timLoginThreadPool;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String[] NICKNAMES = {"tim", "xim", "fim"};

    @Override
    public R loginByPwd(LoginForm loginForm) {
        String username = loginForm.getUsername();
        String password = loginForm.getPassword();
        // 查询
        QueryWrapper<User> u = new QueryWrapper<>();
        u.eq("username", username);
        User user = userMapper.selectOne(u);
        if (user == null) { // 查无此人
            return R.fail().setData("msg", "查无此人");
        }
        if (!password.equals(user.getPassword())) {
            return R.fail().setData("msg", "账号密码不匹配");
        }

        // 检查是否登录过了
        try {
            Boolean b = redisTemplate.hasKey(RedisKey.userTokenKey(user.getUid()));
            if (Boolean.TRUE.equals(b)) {
                return R.fail().setData("msg", "用户已登录,不能重新登录");
            }
        } catch (Exception e) {
            log.error("Redis不可用 {}", e.getMessage());
            return R.fail().setData("msg", "Redis不可用");
        }

        // 生成jwt
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", String.valueOf(user.getUid()));
        claims.put("username", username);
        String tim_token = jwtUtils.generateToken(claims, username);

        // 放入redis-token
        timLoginThreadPool.submit(() -> {
            log.info("用户{}登录成功，生成token成功，放入redis中...", username);
            try {
                // 这里的过期时间和jwt的过期时间一致
                redisTemplate.opsForValue()
                        .set(RedisKey.userTokenKey(user.getUid()), tim_token, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("用户{}登录成功，生成token成功，放入redis中失败...", username);
            }
        });

        return R.success().setData(Token.TOKEN_PREFIX, tim_token);
    }

    @Override
    public R registerByForm(RegisterForm registerForm) {
        if (!registerForm.getPassword().equals(registerForm.getRetryPwd())) {
            return R.fail().setData("msg", "两次密码不一致");
        }
        String username = registerForm.getUsername();
        User user1 = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user1 != null) {
            return R.fail().setData("msg", "用户名已存在");
        }

        User user = new User();
        user.setUsername(registerForm.getUsername());
        user.setPassword(registerForm.getPassword());
        user.setNickname(registerForm.getNickname());

        Date date = new Date();
        user.setCreateTime(date);
        user.setModifyTime(date);
        // 插入数据库
        int insert = userMapper.insert(user);
        if (insert != 1) {
            return R.fail().setData("msg", "插入数据库失败了..请重新注册");
        }
        return R.success().setData("msg", "注册成功!");
    }

    @Override
    public R genUsername() {
        String username = gen();
        if (username == null) {
            return R.fail().setData("msg", "生成用户名失败");
        }
        return R.success().setData("username", username);
    }

    public String gen() {
        int k = 0;
        while ( k++ < 10 ) {
            // 生成一个字符串，以tim、xim、fim其中之一开头，后面跟9位数字，要求生成的字符串不能重复
            long l = System.currentTimeMillis();
            StringBuilder suff = new StringBuilder();
            Random random = new Random();
            // 开头
            suff.append(NICKNAMES[random.nextInt(NICKNAMES.length)]);
            // 随机生成四位数字字符
            for (int i = 0; i < 4; i++) {
                suff.append(random.nextInt(10));
            }
            // 截取时间戳后5位数字
            suff.append(String.valueOf(l).substring(8));

            long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, suff));
            if (count == 0) {
                return suff.toString();
            }
            log.info("第{}次生成用户名失败", k);
        }
        return null;
    }
}
