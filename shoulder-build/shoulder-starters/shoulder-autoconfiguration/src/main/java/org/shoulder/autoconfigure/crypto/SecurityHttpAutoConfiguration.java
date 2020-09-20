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
            // todo 不要覆盖
            securityRestTemplate.setInterceptors(interceptors);
        }
        return securityRestTemplate;
    }
}
