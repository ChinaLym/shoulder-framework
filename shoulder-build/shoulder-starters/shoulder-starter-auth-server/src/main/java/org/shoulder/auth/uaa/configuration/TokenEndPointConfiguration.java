package org.shoulder.auth.uaa.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import org.shoulder.auth.uaa.endpoint.IntrospectEndpoint;
import org.shoulder.auth.uaa.endpoint.JwkSetEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * 默认提供两个 token 端点
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "shoulder.security.token.default-endPoint", havingValue = "enable", matchIfMissing = true)
public class TokenEndPointConfiguration {

    /**
     * 供不透明 token
     */
    @Bean
    @ConditionalOnMissingBean
    public IntrospectEndpoint introspectEndpoint(TokenStore tokenStore) {
        return new IntrospectEndpoint(tokenStore);
    }

    /**
     * 供 JWT with JWK
     */
    @Bean
    @ConditionalOnMissingBean
    public JwkSetEndpoint jwkSetEndpoint(JWKSet jwkSet) {
        return new JwkSetEndpoint(jwkSet);
    }

}
