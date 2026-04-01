package com.feng.rag.model;

import com.feng.rag.model.config.GlobalModelProperties;
import com.feng.rag.model.exception.NotSupportProvider;
import com.feng.rag.model.qwen.QwenModel;
import com.feng.rag.model.siliconflow.SiliconflowModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: txf
 * @Date: 2026/3/24
 */
@Slf4j
@Component
public class ModelFactory {
    private final GlobalModelProperties modelProperties;
    public ModelFactory( GlobalModelProperties globalModelProperties) {
        this.modelProperties = globalModelProperties;
    }

    private final Map<String, AbstractModel> modelMap = new HashMap<>();

    public AbstractModel getModel(String providerName) {
        return modelMap.get(providerName);
    }

    @PostConstruct
    public void init() {
        log.info("开始根据各种配置创建模型...");
        modelProperties.getProviders().forEach((providerName, providerConfig) -> {
            modelMap.put(providerName, buildModel(providerName, providerConfig));
        });
    }

    private AbstractModel buildModel(String providerName, GlobalModelProperties.ProviderConfig providerConfig) {
        // 根据不同的提供商创建不同的模型
        return switch (providerName) {
            case SiliconflowModel.SILICONFLOW -> new SiliconflowModel(providerConfig);
            case QwenModel.QWEN -> new QwenModel(providerConfig);
            default -> throw new NotSupportProvider("不支持的提供商: " + providerName);
        };
    }
}
