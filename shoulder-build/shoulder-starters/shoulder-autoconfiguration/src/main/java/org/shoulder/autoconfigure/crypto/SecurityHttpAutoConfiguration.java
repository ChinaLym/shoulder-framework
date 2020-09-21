package org.shoulder.autoconfigure.crypto;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.autoconfigure.http.HttpAutoConfiguration;
import org.shoulder.crypto.negotiation.http.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.http.SensitiveDateEncryptMessageConverter;
import org.shoulder.crypto.negotiation.interceptor.DecryptSecurityResponseClientInterceptor;
import org.shoulder.crypto.negotiation.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * http（RestTemplate） 相关配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SecurityRestTemplate.class)
@AutoConfigureAfter(value = {HttpAutoConfiguration.class, TransportCryptoAutoConfiguration.class})
public class SecurityHttpAutoConfiguration {

    @Bean
    @ConditionalOnClass
    public SecurityRestTemplate securityRestTemplate(TransportNegotiationService transportNegotiationService,
                                                     TransportCryptoUtil cryptoUtil, @Nullable List<ClientHttpRequestInterceptor> interceptors) {
        SecurityRestTemplate securityRestTemplate = new SecurityRestTemplate(transportNegotiationService, cryptoUtil);
        // ClientHttpRequestInterceptor
        if (CollectionUtils.isNotEmpty(interceptors)) {
            List<ClientHttpRequestInterceptor> existConverters = securityRestTemplate.getInterceptors();
            // 简单做差值去重，这里用的 == 比较
            Collection<ClientHttpRequestInterceptor> toAdd = CollectionUtils.subtract(interceptors, existConverters);

            if (CollectionUtils.isNotEmpty(toAdd)) {
                List<ClientHttpRequestInterceptor> newInterceptors = new ArrayList<>(existConverters.size() + toAdd.size());
                newInterceptors.add(new DecryptSecurityResponseClientInterceptor(cryptoUtil));
                newInterceptors.addAll(existConverters);
                newInterceptors.addAll(toAdd);
                securityRestTemplate.setInterceptors(newInterceptors);
            }
        }
        // HttpMessageConverter
        List<HttpMessageConverter<?>> converterList = securityRestTemplate.getMessageConverters();
        boolean containsSecurityConverter = false;
        for (HttpMessageConverter<?> httpMessageConverter : converterList) {
            if (httpMessageConverter instanceof SensitiveDateEncryptMessageConverter) {
                containsSecurityConverter = true;
                break;
            }
        }
        if (!containsSecurityConverter) {
            List<HttpMessageConverter<?>> newConverters = new ArrayList<>(converterList.size() + 1);
            // 先加必须的，常用的
            newConverters.add(new SensitiveDateEncryptMessageConverter());
            // 再加Spring自带的
            newConverters.addAll(converterList);
            securityRestTemplate.setMessageConverters(newConverters);
        }
        return securityRestTemplate;
    }
}
