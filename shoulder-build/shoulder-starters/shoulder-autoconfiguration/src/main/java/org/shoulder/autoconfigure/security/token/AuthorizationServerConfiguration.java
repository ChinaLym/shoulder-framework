package org.shoulder.autoconfigure.security.token;

import org.shoulder.security.SecurityConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.sql.DataSource;

/**
 * 旧授权服务器（spring-security-oauth2）的一个实例，它使用一个单独的、不旋转的密钥并公开一个JWK端点。
 * <p>
 * 更多详情：
 * <a target="_blank" href="https://docs.spring.io/spring-security-oauth2-boot/docs/current-SNAPSHOT/reference/htmlsingle/">
 * Spring Security OAuth Autoconfig's documentation
 * </a>
 *
 * @author Josh Cummings
 * @since 5.1
 */
@EnableAuthorizationServer
@ConditionalOnClass(SecurityConst.class)
@Configuration
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired(required = false)
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * 配置授权服务器的校验策略
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 配置 获取用于验签 token 的公钥 Key uri 的访问权限
        security.
            tokenKeyAccess("permitAll()")
            // 验证accessToken uri 的访问权限
            .checkTokenAccess("isAuthenticated()");
    }

    /**
     * 配置本授权服务器允许的客户端凭证信息（clientId | clientSecret）
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
        throws Exception {
        // clientDetailsService 这里获取的是一个代理对象 P，然后把这个注册到 P 上，会导致死循环
        //clients.withClientDetails(clientDetailsService);

        if (dataSource != null) {
            clients
                .jdbc(dataSource)
            //.passwordEncoder()
            ;
        } else {
            clients.inMemory();
        }
    }

    /**
     * 配置端点信息
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        // @formatter:off
        endpoints
                .authenticationManager(this.authenticationManager)
                .tokenStore(tokenStore)
                .userDetailsService(userDetailsService)
        ;

        if (this.jwtAccessTokenConverter != null) {
            endpoints
                    .accessTokenConverter(jwtAccessTokenConverter);
        }
        // @formatter:on
    }
}
