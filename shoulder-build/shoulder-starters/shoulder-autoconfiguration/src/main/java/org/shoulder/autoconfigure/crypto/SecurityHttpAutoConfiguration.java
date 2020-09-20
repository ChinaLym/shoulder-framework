package org.shoulder.autoconfigure.crypto;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.autoconfigure.http.HttpAutoConfiguration;
import org.shoulder.crypto.negotiation.http.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
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
        if (CollectionUtils.isNotEmpty(interceptors)) {
            List<ClientHttpRequestInterceptor> existConverters = securityRestTemplate.getInterceptors();
            // 简单做差值去重，这里用的 == 比较
            Collection<ClientHttpRequestInterceptor> toAdd = CollectionUtils.subtract(interceptors, existConverters);

            if (CollectionUtils.isNotEmpty(toAdd)) {
                List<ClientHttpRequestInterceptor> newInterceptors = new ArrayList<>(existConverters.size() + toAdd.size());
                newInterceptors.addAll(existConverters);
                newInterceptors.addAll(toAdd);
                securityRestTemplate.setInterceptors(newInterceptors);
            }
        }
        return securityRestTemplate;
    }
}
