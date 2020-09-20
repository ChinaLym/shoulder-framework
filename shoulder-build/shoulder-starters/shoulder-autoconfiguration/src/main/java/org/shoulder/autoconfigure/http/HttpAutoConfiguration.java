package org.shoulder.autoconfigure.http;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

/**
 * http（RestTemplate） 相关配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestTemplate.class)
@AutoConfigureAfter(value = RestTemplateAutoConfiguration.class)
//@NotReactiveWebApplicationCondition
public class HttpAutoConfiguration {

    @Bean
    public RestTemplate restTemplate(@NonNull RestTemplateBuilder builder) {
        return builder.build();
    }

}
