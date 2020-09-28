package org.shoulder.autoconfigure.security.token;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.autoconfigure.security.AuthenticationHandlerConfig;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.AuthenticationType;
import org.shoulder.security.authentication.BeforeAuthEndpoint;
import org.shoulder.security.authentication.handler.TokenAuthenticationSuccessHandler;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * token 认证模式时 bean 配置：几个默认处理器
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SecurityConst.class)
@AutoConfigureBefore(AuthenticationHandlerConfig.class)
@EnableConfigurationProperties(TokenProperties.class)
@ConditionalOnAuthType(type = AuthenticationType.TOKEN)
public class TokenAuthBeanConfig {

    /**
     * 待认证请求处理器
     *
     * @return 待认证请求处理器
     */
    @Bean
    @ConditionalOnProperty(value = "shoulder.security.auth.browser.default-endpoint.enable", havingValue = "true", matchIfMissing = true)
    public BeforeAuthEndpoint beforeAuthEndpoint() {
        return new BeforeAuthEndpoint(null);
    }

    /**
     * 认证成功处理器
     * ClientDetailsService、AuthorizationServerTokenServices 由 {@link EnableAuthorizationServer} 提供
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationSuccessHandler.class)
    public AuthenticationSuccessHandler tokenAuthenticationSuccessHandler(ClientDetailsService clientDetailsService,
                                                                          AuthorizationServerTokenServices authorizationServerTokenServices) {
        return new TokenAuthenticationSuccessHandler(clientDetailsService, authorizationServerTokenServices);
    }


    /**
     * TokenStore 相关配置
     */
    /*@Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(TokenStore.class)
    public static class TokenStoreConfig {

        *//**
     * 使用 redis 作为 token 存储
     *//*
        @Configuration(proxyBeanMethods = false)
        @ConditionalOnProperty(prefix = "lym.security.oauth2", name = "tokenStore", havingValue = "redis")
        public static class RedisConfig {

            @Bean
            public TokenStore redisTokenStore(RedisConnectionFactory redisConnectionFactory) {
                return new RedisTokenStore(redisConnectionFactory);
            }

        }

        *//**
     * 使用 jwt 时的配置，默认生效
     *//*
        @Configuration(proxyBeanMethods = false)
        @ConditionalOnProperty(prefix = "lym.security.oauth2", name = "tokenStore", havingValue = "jwt", matchIfMissing = true)
        public static class JwtConfig {

            @Bean
            public TokenStore jwtTokenStore() {
                return new JwtTokenStore(jwtAccessTokenConverter());
            }

            @Bean
            public JwtAccessTokenConverter jwtAccessTokenConverter(SecurityProperties securityProperties){
                JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
                converter.setSigningKey(securityProperties.getOauth2().getJwtSigningKey());
                return converter;
            }

            @Bean
            @ConditionalOnMissingBean(TokenEnhancer.class)
            public TokenEnhancer jwtTokenEnhancer(){
                return new TokenJwtEnhancer();
            }

        }

    }*/

}
