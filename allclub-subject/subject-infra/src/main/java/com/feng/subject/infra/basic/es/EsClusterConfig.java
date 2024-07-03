package com.feng.subject.infra.basic.es;

import lombok.Data;

import java.io.Serializable;

/**
 * es集群配置信息类
 * 
 * @author: txf
 * @date: 2023/12/17
 */
@Data
public class EsClusterConfig implements Serializable {

    /**
     * 集群名称
     */
    private String name;

    /**
     * 集群节点
     */
    private String nodes; // 可以有多个ip:port，中间用逗号分割开来
    /*
     用户名
     */
    private String username;
    /*
     密码
     */
    private String password;

}
