package com.feng.tim.auth;

import com.feng.tim.auth.jwt.JwtUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/10/12
 */
@SpringBootTest
@SuppressWarnings("all")
public class AuthTestApplication {
    @Resource
    private JwtUtils jwtUtils;

    @Test
    public void contextLoads() {
        String username = "tim123456789";
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", String.valueOf(1));
        claims.put("username", username);
        String tim_token = jwtUtils.generateToken(claims, username);
        System.out.println(tim_token + "\n" + "==============\n");

        TokenUser tokenUser = jwtUtils.getClaimFromToken(tim_token, (c) -> {
            return new TokenUser((String) c.get("uid"), (String) c.get("username"));
        });
        System.out.println("解析后的user" + tokenUser);

        String claimFromToken = jwtUtils.getClaimFromToken(tim_token, (c) -> {
            return c.getSubject();
        });
        System.out.println("解析后的username" + claimFromToken);
    }

    static class TokenUser{
        private String username;
        private Long uid;
        public TokenUser() {
        }
        public TokenUser(String uid, String username) {
            this.uid = Long.valueOf(uid);
            this.username = username;
        }
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public Long getUid() {
            return uid;
        }
        public void setUid(Long uid) {
            this.uid = uid;
        }

        @Override
        public String toString() {
            return "TokenUser{" +
                    "username='" + username + '\'' +
                    ", uid=" + uid +
                    '}';
        }
    }
}
