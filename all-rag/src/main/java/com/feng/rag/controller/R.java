package com.feng.rag.controller;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 统一的返回结果
 * @Author: txf
 * @Date: 2026/3/25
 */
@Data
public class R implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private Map<String, Object> data = new HashMap<>();

    public R() {
    }

    public R(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 添加数据（链式调用）
     *
     * @param key   键
     * @param value 值
     * @return 当前R对象
     */
    public R add(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 设置data（覆盖原有数据）
     *
     * @param data 数据Map
     * @return 当前R对象
     */
    public R data(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    /**
     * 成功返回结果
     */
    public static R ok() {
        return new R(200, "success");
    }

    /**
     * 成功返回结果
     *
     * @param message 提示信息
     */
    public static R ok(String message) {
        return new R(200, message);
    }

    /**
     * 失败返回结果
     */
    public static R error() {
        return new R(500, "error");
    }

    /**
     * 失败返回结果
     *
     * @param message 错误信息
     */
    public static R error(String message) {
        return new R(500, message);
    }

    /**
     * 失败返回结果
     *
     * @param code    状态码
     * @param message 错误信息
     */
    public static R error(Integer code, String message) {
        return new R(code, message);
    }
}
