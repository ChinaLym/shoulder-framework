package org.shoulder.autoconfigure.http;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;

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

    @ConditionalOnMissingBean
    @Bean
    public RestTemplate restTemplate(@Nonnull RestTemplateBuilder builder) {
        return builder.build();
    }

}
