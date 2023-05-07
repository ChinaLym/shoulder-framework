package org.shoulder.autoconfigure.crypto;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.autoconfigure.http.HttpAutoConfiguration;
import org.shoulder.crypto.negotiation.cache.NegotiationResultCache;
import org.shoulder.crypto.negotiation.support.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.support.client.SensitiveRequestEncryptMessageConverter;
import org.shoulder.crypto.negotiation.support.client.SensitiveResponseDecryptInterceptor;
import org.shoulder.crypto.negotiation.support.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.shoulder.http.AppIdExtractor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * http（RestTemplate） 相关配置
 *
 * @author lym
 */
@AutoConfiguration(after = {HttpAutoConfiguration.class, TransportCryptoAutoConfiguration.class})
@ConditionalOnClass(SecurityRestTemplate.class)
public class SecurityHttpAutoConfiguration {

    @Bean
    @ConditionalOnClass
    public SecurityRestTemplate securityRestTemplate(TransportNegotiationService transportNegotiationService,
                                                     TransportCryptoUtil cryptoUtil,
                                                     @Nullable List<ClientHttpRequestInterceptor> interceptors,
                                                     NegotiationResultCache negotiationResultCache, AppIdExtractor appIdExtractor) {

        SecurityRestTemplate securityRestTemplate = new SecurityRestTemplate(transportNegotiationService, cryptoUtil, negotiationResultCache, appIdExtractor);
        // ClientHttpRequestInterceptor
        if (CollectionUtils.isNotEmpty(interceptors)) {
            List<ClientHttpRequestInterceptor> existConverters = securityRestTemplate.getInterceptors();
            // 简单做差值去重，这里用的 == 比较
            Collection<ClientHttpRequestInterceptor> toAdd = CollectionUtils.subtract(interceptors, existConverters);

            if (CollectionUtils.isNotEmpty(toAdd)) {
                List<ClientHttpRequestInterceptor> newInterceptors = new ArrayList<>(existConverters.size() + toAdd.size());
                // 根据 order 排序，order 小的在前面
                newInterceptors.add(new SensitiveResponseDecryptInterceptor(cryptoUtil, negotiationResultCache, appIdExtractor));
                newInterceptors.addAll(existConverters);
                newInterceptors.addAll(toAdd);
                newInterceptors.sort((interceptor1, interceptor2) -> {
                    boolean isOrdered1 = interceptor1 instanceof Ordered;
                    boolean isOrdered2 = interceptor2 instanceof Ordered;
                    if (isOrdered1 && isOrdered2) {
                        return Integer.compare(((Ordered) interceptor1).getOrder(), ((Ordered) interceptor2).getOrder());
                    } else {
                        return isOrdered1 ? -1 : 1;
                    }
                });
                securityRestTemplate.setInterceptors(newInterceptors);
            }
        }
        // HttpMessageConverter
        List<HttpMessageConverter<?>> converterList = securityRestTemplate.getMessageConverters();
        boolean containsSecurityConverter = false;
        for (HttpMessageConverter<?> httpMessageConverter : converterList) {
            if (httpMessageConverter instanceof SensitiveRequestEncryptMessageConverter) {
                containsSecurityConverter = true;
                break;
            }
        }
        if (!containsSecurityConverter) {
            List<HttpMessageConverter<?>> newConverters = new ArrayList<>(converterList.size() + 1);
            // 先加必须的，常用的
            newConverters.add(new SensitiveRequestEncryptMessageConverter());
            // 再加Spring自带的
            newConverters.addAll(converterList);
            securityRestTemplate.setMessageConverters(newConverters);
        }
        return securityRestTemplate;
    }
}
