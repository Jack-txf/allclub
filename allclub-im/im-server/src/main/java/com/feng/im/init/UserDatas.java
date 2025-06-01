package com.feng.im.init;


import com.feng.im.api.model.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 用户数据--模拟redis、mysql的数据操作
 * @Author: txf
 * @Date: 2025/5/4
 */
@Component
public class UserDatas {
    private Map<String, User> userData = null;

    private Map<String, User> hasLoginUser = null; // 已登录的用户: uid -- User的映射
    private Map<String, String> tokenMap = null; // token与uid的映射

    public User getUserById( String uid ) {
        return userData.get(uid);
    }
    public User getUserByToken( String token ) {
        return userData.values().stream()
                .filter(user -> user.getToken().equals(token)).findFirst().
                orElse(null);
    }

    public boolean logoutUser( String uid ) {
        if (!hasLoginUser.containsKey(uid)) {
            return false;
        }
        hasLoginUser.remove(uid);
        User user = userData.get(uid);
        tokenMap.remove(user.getToken());
        return true;
    }

    public boolean loginUser( String uid ) {
        if (hasLoginUser.containsKey(uid)) {
            return false;
        }
        hasLoginUser.put(uid, userData.get(uid));
        User user = userData.get(uid);
        tokenMap.put(user.getToken(), user.getUid());
        return true;
    }
    // 通过token得到已经登录的用户
    public User parseUserByToken( String token ) {
        String uid = tokenMap.get(token);
        return hasLoginUser.get(uid);
    }

    @PostConstruct
    public void init() {
        List<User> users = Arrays.asList(
                new User("1", "张三", "zhangsan", "123456", "zhangsan@163.com", "token1"),
                new User("2", "李四", "lisi", "123456", "lisi@163.com", "token2"),
                new User("3", "王五", "wangwu", "123456", "wangwu@163.com", "token3")
        );
        userData = users.stream().collect(
                Collectors.toMap(User::getUid, Function.identity())
        );
        hasLoginUser = new ConcurrentHashMap<>();
        tokenMap = new ConcurrentHashMap<>();
    }
}
