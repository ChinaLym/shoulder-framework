package org.shoulder.autoconfigure.http;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.http.interceptor.BaseRestTemplateLogInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义拦截器 + 缓存响应体
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(value = {RestTemplate.class, BaseRestTemplateLogInterceptor.class})
public class RestTemplateCustomAutoConfiguration implements InitializingBean {

    @Lazy
    @Autowired(required = false)
    private List<ClientHttpRequestInterceptor> interceptors;

    @Lazy
    @Autowired(required = false)
    private List<RestTemplate> restTemplateList;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(restTemplateList) && CollectionUtils.isNotEmpty(interceptors)) {
            restTemplateList.forEach(this::customRestTemplate);
        }
    }

    private void customRestTemplate(RestTemplate restTemplate) {
        // 由于自动记录响应日志。故需要缓存响应体
        if (!(restTemplate.getRequestFactory() instanceof BufferingClientHttpRequestFactory)) {
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        }
        // 自定义拦截器注入
        List<ClientHttpRequestInterceptor> existingInterceptors = restTemplate.getInterceptors();
        interceptors.forEach(interceptor -> {
            if (!existingInterceptors.contains(interceptor)) {
                List<ClientHttpRequestInterceptor> newInterceptors = new ArrayList<>();
                newInterceptors.add(interceptor);
                newInterceptors.addAll(existingInterceptors);
                restTemplate.setInterceptors(newInterceptors);
            }
        });
    }

}
