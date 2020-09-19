package org.shoulder.autoconfigure.crypto;

import org.shoulder.autoconfigure.http.HttpAutoConfiguration;
import org.shoulder.crypto.negotiation.http.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                                                     TransportCryptoUtil cryptoUtil) {
        return new SecurityRestTemplate(transportNegotiationService, cryptoUtil);
    }
}
