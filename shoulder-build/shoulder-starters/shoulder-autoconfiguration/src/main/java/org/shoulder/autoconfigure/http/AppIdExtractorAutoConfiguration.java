package org.shoulder.autoconfigure.http;

import org.shoulder.http.AppIdExtractor;
import org.shoulder.http.ShoulderDslAppIdExtractor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 从 url 中提取 appId 信息
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AppIdExtractor.class)
@ConditionalOnMissingBean(AppIdExtractor.class)
public class AppIdExtractorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppIdExtractor appIdExtractor() {
        return new ShoulderDslAppIdExtractor();
    }
}
