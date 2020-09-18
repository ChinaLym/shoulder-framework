package org.shoulder.autoconfigure.http;

import org.shoulder.http.interceptor.BaseRestTemplateLogInterceptor;
import org.shoulder.http.interceptor.RestTemplateColorfulLogInterceptor;
import org.shoulder.http.interceptor.RestTemplateJsonLogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnClass(BaseRestTemplateLogInterceptor.class)
@ConditionalOnMissingBean(BaseRestTemplateLogInterceptor.class)
public class RestTemplateLogAutoConfiguration {
    /**
     * 多行彩色形式【用于开发态】日志打在多行，且带颜色，代码跳转
     */
    @Bean
    @Order(value = 0)
    @ConditionalOnProperty(name = "shoulder.http.logRequest", havingValue = "colorful", matchIfMissing = true)
    public ClientHttpRequestInterceptor restTemplateColorfulLogInterceptor(
        @Value("${shoulder.http.log.useCallerLogger:true}") boolean useCallerLogger,
        @Value("${shoulder.http.log.logTillResponse:true}") boolean logTillResponse
    ) {
        return new RestTemplateColorfulLogInterceptor(useCallerLogger, logTillResponse);
    }

    /**
     * Json 形式【用于生产态】日志打在一行中
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "shoulder.http.logRequest", havingValue = "json")
    public ClientHttpRequestInterceptor restTemplateJsonLogInterceptor(
        @Value("${shoulder.http.log.logTillResponse:true}") boolean logTillResponse
    ) {
        return new RestTemplateJsonLogInterceptor(logTillResponse);
    }
}
