package org.shoulder.autoconfigure.security.token;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.autoconfigure.security.AuthenticationHandlerConfig;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.crypto.asymmetric.AsymmetricCipher;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricCipher;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.AuthenticationType;
import org.shoulder.security.authentication.BeforeAuthEndpoint;
import org.shoulder.security.authentication.handler.json.BasicAuthorizationTokenAuthenticationSuccessHandler;
import org.shoulder.security.authentication.token.SimpleTokenIntrospector;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerTokenServicesConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerTokenServicesConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

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
public class TokenAuthBeanConfiguration {

    /**
     * 待认证请求处理器
     *
     * @return 待认证请求处理器
     */
    //@Bean
    @ConditionalOnProperty(value = "shoulder.security.auth.browser.default-endpoint.enable", havingValue = "true", matchIfMissing = true)
    public BeforeAuthEndpoint beforeAuthEndpoint() {
        return new BeforeAuthEndpoint(null);
    }

    /**
     * token 认证成功处理器
     * ClientDetailsService、AuthorizationServerTokenServices 由 {@link EnableAuthorizationServer} 提供
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationSuccessHandler tokenAuthenticationSuccessHandler(ClientDetailsService clientDetailsService,
                                                                          AuthorizationServerTokenServices authorizationServerTokenServices) {
        return new BasicAuthorizationTokenAuthenticationSuccessHandler(clientDetailsService, authorizationServerTokenServices);
    }


    @Bean
    @ConditionalOnMissingBean
    public DefaultTokenServices defaultTokenServices(TokenStore tokenStore) {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore);
        return tokenServices;
    }


    /**
     * token 认证器
     *
     * @param opaqueTokenIntrospector 具体的认证逻辑
     */
    @Bean
    @ConditionalOnMissingBean
    public OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider(OpaqueTokenIntrospector opaqueTokenIntrospector) {
        return new OpaqueTokenAuthenticationProvider(opaqueTokenIntrospector);
    }

    /**
     * 默认 token 认证逻辑
     *
     * @param tokenServices        tokenService {@link #defaultTokenServices(TokenStore)}
     * @param clientDetailsService 由使用者注入
     * @return token 认证默认实现
     */
    @Bean
    @ConditionalOnMissingBean
    public SimpleTokenIntrospector simpleTokenIntrospector(ResourceServerTokenServices tokenServices, ClientDetailsService clientDetailsService) {
        LoggerFactory.getLogger(getClass()).warn("use SimpleTokenIntrospector, recommend inject a customized OpaqueTokenIntrospector");
        return new SimpleTokenIntrospector(tokenServices, clientDetailsService);
    }


    /**
     * TokenStore 相关配置
     *
     * @see TokenStore 根据配置项，对 spring security 提供的存储做了自动装配，替代 dsl
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(TokenStore.class)
    public static class TokenStoreConfig {


        // ==================== 适用于 opaqueToken =========================

        /**
         * 使用 redis 作为 token 存储
         */
        @Configuration(proxyBeanMethods = false)
        @ConditionalOnMissingBean(TokenStore.class)
        @ConditionalOnProperty(prefix = "shoulder.security.token", name = "store", havingValue = "redis")
        public static class RedisTokenStoreConfig {
            @Bean
            public TokenStore redisTokenStore(RedisConnectionFactory redisConnectionFactory) {
                return new RedisTokenStore(redisConnectionFactory);
            }
        }

        /**
         * 使用 db 作为 token 存储
         */
        @Configuration(proxyBeanMethods = false)
        @ConditionalOnMissingBean(TokenStore.class)
        @ConditionalOnProperty(prefix = "shoulder.security.token", name = "store", havingValue = "jdbc")
        public static class JdbcTokenStoreConfig {
            @Bean
            public TokenStore jdbcTokenStore(DataSource dataSource) {
                return new JdbcTokenStore(dataSource);
            }
        }

        /**
         * 使用内存 token 存储【由于非持久化，每次启动都会丢失，故仅供测试】
         */
        @Configuration(proxyBeanMethods = false)
        @ConditionalOnMissingBean(TokenStore.class)
        @ConditionalOnProperty(prefix = "shoulder.security.token", name = "store", havingValue = "memory",
            matchIfMissing = true)
        public static class InMemoryTokenStoreConfig {
            @Bean
            public TokenStore inMemoryTokenStore() {
                return new InMemoryTokenStore();
            }
        }


        // ==================== 适用于 jwt =========================

        /**
         * 使用 jwt 时的配置，默认生效
         */
        @Configuration(proxyBeanMethods = false)
        @ConditionalOnMissingBean(TokenStore.class)
        @ConditionalOnProperty(prefix = "shoulder.security.token", name = "store", havingValue = "jwt", matchIfMissing = true)
        public static class JwtTokenStoreConfig {

            /**
             * 若为 jwk 或者 自定义 jwt，注入自己的 jwtTokenEnhancer 即可
             *
             * @see AuthorizationServerTokenServicesConfiguration.JwtTokenServicesConfiguration#jwtTokenEnhancer 认证服务器
             * @see ResourceServerTokenServicesConfiguration.JwtTokenServicesConfiguration#jwtTokenEnhancer 资源服务器（一般不生效）
             */
            @Bean
            public TokenStore jwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
                return new JwtTokenStore(jwtTokenEnhancer);
            }

            /**
             * 用于将 jwt 解码，转为实际 token 与其对应信息的
             */
            @Bean
            @ConditionalOnMissingBean
            public JwtAccessTokenConverter accessTokenConverter(KeyPair keyPair, @Nullable UserAuthenticationConverter userAuthenticationConverter) {
                JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

                // 非 jwk 可为空
                converter.setKeyPair(keyPair);

                DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
                if (userAuthenticationConverter != null) {
                    accessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
                }
                converter.setAccessTokenConverter(accessTokenConverter);

                return converter;
            }

            /**
             * keyPair，没有则随机生成一个，id 为第一个，利用 buildKeyPair 幂等性
             */
            @Bean
            @ConditionalOnMissingBean
            public KeyPair keyPair(List<String> jwtKeyPairIds, KeyPairCache keyPairCache) throws KeyPairException {
                jwtKeyPairIds = List.of("jwk");
                AsymmetricCipher rsa2048 = DefaultAsymmetricCipher.rsa2048(keyPairCache);
                DefaultAsymmetricCipher.rsa2048(keyPairCache).buildKeyPair(jwtKeyPairIds.get(0));
                return rsa2048.getKeyPair(jwtKeyPairIds.get(0));
            }

            /**
             * todo 【优化】jwk 如果配置配了，从配置中拿，而非随机生成，便于使用者使用固定的
             * 目前不支持配置，但使用者可以通过注入 TokenStore 来实现自己的方案
             */
            @Bean
            @ConditionalOnMissingBean
            public JWKSet jwkSet(List<String> jwtKeyPairIds, KeyPairCache keyPairCache) throws KeyPairException {
                jwtKeyPairIds = List.of("jwk");
                AsymmetricCipher rsa2048 = DefaultAsymmetricCipher.rsa2048(keyPairCache);
                List<JWK> keys = new ArrayList<>(jwtKeyPairIds.size());
                for (String jwtKeyPairId : jwtKeyPairIds) {
                    rsa2048.buildKeyPair(jwtKeyPairId);
                    RSAPublicKey publicKey = (RSAPublicKey) rsa2048.getKeyPair(jwtKeyPairId).getPublic();
                    RSAKey key = new RSAKey.Builder(publicKey).build();
                    keys.add(key);
                }
                return new JWKSet(keys);
            }

        }

    }

}
