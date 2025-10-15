package com.feng.tim.ws.obj;

import java.util.Objects;

public class TokenUser {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TokenUser tokenUser = (TokenUser) o;
        return Objects.equals(username, tokenUser.username)
                && Objects.equals(uid, tokenUser.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, uid);
    }
}