package com.feng.circle.server.config;

import com.feng.circle.server.sensitive.WordContext;
import com.feng.circle.server.sensitive.WordFilter;
import com.feng.circle.server.service.SensitiveWordsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensitiveConfig {

    // 注意，这里有个service哦
    @Bean
    public WordContext wordContext(SensitiveWordsService service) {
        return new WordContext(true, service);
    }

    @Bean
    public WordFilter wordFilter(WordContext wordContext) {
        return new WordFilter(wordContext);
    }

}
