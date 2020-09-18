package org.shoulder.autoconfigure.crypto;

import org.shoulder.autoconfigure.http.HttpAutoConfiguration;
import org.shoulder.crypto.negotiation.http.SecurityRestTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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


}
