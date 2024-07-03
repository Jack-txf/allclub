package com.feng.subject.infra.basic.es;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
/*
配置类：读取配置文件自定义的属性，支持集群，节点等等一些信息
方式：
1. @Configuration + @ConfigurationProperties + @Data（必须提供set方法）
2. @Configuration +  @Value
 */
@Getter
@Component
@ConfigurationProperties(prefix = "es.cluster")
public class EsConfigProperties {

    // 集群配置们（管理多个集群）
    private List<EsClusterConfig> esConfigs = new ArrayList<>();

    public void setEsConfigs(List<EsClusterConfig> esConfigs) {
        this.esConfigs = esConfigs;
    }
}
