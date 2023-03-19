package org.shoulder.autoconfigure.security.token;

import com.nimbusds.jose.jwk.JWKSet;
import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.AuthenticationType;
import org.shoulder.security.authentication.endpoint.IntrospectEndpoint;
import org.shoulder.security.authentication.endpoint.JwkSetEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * 默认提供两个 token 端点
 *
 * @author lym
 */
@AutoConfiguration
@ConditionalOnClass(SecurityConst.class)
@EnableConfigurationProperties(TokenProperties.class)
@ConditionalOnProperty(value = "shoulder.security.token.store", havingValue = "jwt", matchIfMissing = true)
@ConditionalOnAuthType(type = AuthenticationType.TOKEN)
public class TokenEndPointConfiguration {

    /**
     * 供不透明 token
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "shoulder.security.token.jwk", havingValue = "false")
    public IntrospectEndpoint introspectEndpoint(TokenStore tokenStore) {
        return new IntrospectEndpoint(tokenStore);
    }

    /**
     * 供 JWT with JWK
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "shoulder.security.token.store", havingValue = "jwt")
    public JwkSetEndpoint jwkSetEndpoint(JWKSet jwkSet) {
        return new JwkSetEndpoint(jwkSet);
    }

}
