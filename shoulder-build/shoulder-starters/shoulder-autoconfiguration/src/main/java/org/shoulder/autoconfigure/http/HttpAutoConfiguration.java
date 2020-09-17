package org.shoulder.autoconfigure.http;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.http.AppIdExtractor;
import org.shoulder.http.ShoulderDslAppIdExtractor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;

/**
 * http（RestTemplate） 相关配置
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass(RestTemplate.class)
@AutoConfigureAfter(value = {RestTemplateAutoConfiguration.class, RestTemplateLogAutoConfiguration.class})
public class HttpAutoConfiguration {

    @Bean
    @ConditionalOnClass(name = "org.shoulder.http.AppIdExtractor")
    @ConditionalOnMissingBean
    public AppIdExtractor appIdExtractor() {
        return new ShoulderDslAppIdExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(@Nullable RestTemplateBuilder builder, @Nullable List<ClientHttpRequestInterceptor> interceptors) {
        RestTemplate restTemplate;
        if (builder == null) {
            restTemplate = new RestTemplate();
        } else {
            restTemplate = builder.build();
        }
        if (CollectionUtils.isNotEmpty(interceptors)) {
            // 若非空则尝试注册
            List<ClientHttpRequestInterceptor> registeredInterceptors = restTemplate.getInterceptors();
            Collection<ClientHttpRequestInterceptor> shouldRegister = interceptors;
            if (CollectionUtils.isEmpty(registeredInterceptors)) {
                // 当且仅当非空则取差值，避免重复注册，同时避免无效去重
                shouldRegister = CollectionUtils.subtract(interceptors, registeredInterceptors);
            }
            registeredInterceptors.addAll(shouldRegister);
        }
        return restTemplate;
    }

}
