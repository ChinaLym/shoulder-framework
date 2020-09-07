package org.shoulder.autoconfigure.http;

import org.shoulder.http.AppIdExtractor;
import org.shoulder.http.ShoulderDslAppIdExtractor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * http 相关配置
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass(AppIdExtractor.class)
public class HttpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppIdExtractor appIdExtractor() {
        return new ShoulderDslAppIdExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
