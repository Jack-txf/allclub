package com.feng.tim.auth.common;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Williams_Tian
 * @CreateDate 2024/8/1
 */
@Getter
public class R implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;
    private final Map<String, Object> data;

    private R() {
        data = new HashMap<>();
    }

    public static R success() {
        R r = new R();
        r.code = 200;
        r.data.put("msg", "响应成功！");
        return r;
    }

    public static R success(Integer code) {
        R r = new R();
        r.code = code;
        r.data.put("msg", "响应成功！");
        return r;
    }

    public static R fail() {
        R r = new R();
        r.code = 500;
        r.data.put("msg", "响应失败！");
        return r;
    }

    public static R fail(Integer code) {
        R r = new R();
        r.code = code;
        r.data.put("msg", "响应失败！");
        return r;
    }

    public static R fail(int code, String s) {
        R r = new R();
        r.code = code;
        r.data.put("msg", s);
        return r;
    }

    public R setData( String key, Object value ) {
        this.data.put(key, value);
        return this;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setData(Map<String, Object> data) {
        this.data.putAll(data);
    }
}
