package org.shoulder.autoconfigure.http;

import org.shoulder.http.BaseRestTemplateLogInterceptor;
import org.shoulder.http.RestTemplateColorfulLogInterceptor;
import org.shoulder.http.RestTemplateJsonLogInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * 使用 RestTemplate 发起请求时，记录请求日志
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnMissingBean(BaseRestTemplateLogInterceptor.class)
public class RestTemplateLogAutoConfiguration {
    /**
     * 多行彩色形式【用于开发态】日志打在多行，且带颜色，代码跳转
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.http.logRequest", havingValue = "colorful", matchIfMissing = true)
    public ClientHttpRequestInterceptor restTemplateColorfulLogInterceptor() {
        return new RestTemplateColorfulLogInterceptor();
    }

    /**
     * Json 形式【用于生产态】日志打在一行中
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "shoulder.http.logRequest", havingValue = "json")
    public ClientHttpRequestInterceptor restTemplateJsonLogInterceptor() {
        return new RestTemplateJsonLogInterceptor();
    }
}
