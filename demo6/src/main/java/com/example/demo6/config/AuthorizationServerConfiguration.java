package com.example.demo6.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.LinkedHashMap;
import java.util.Map;

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
@Configuration
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    private KeyPair keyPair;

    /**
     * 默认使用 jwt
     */
    @Autowired
    @Value("${security.oauth2.authorizationserver.jwt.enabled:false}")
    private boolean jwtEnabled;

    //@Autowired(required = false)
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

        //clients.jdbc(dataSource);

        // @formatter:off
        //*
		// 创建默认的 clientDetail
		clients.inMemory()
				.withClient("reader")
					.secret("secret")
					.authorizedGrantTypes("password")
					.scopes("message:read")
					.accessTokenValiditySeconds(600_000_000)
				.and()
					.withClient("writer")
					.secret("secret")
					.authorizedGrantTypes("password")
					.scopes("message:write")
					.accessTokenValiditySeconds(600_000_000)
				.and()
					.withClient("noscopes")
					.secret("secret")
					.authorizedGrantTypes("password")
					.scopes("none")
					.accessTokenValiditySeconds(600_000_000)
				.and()
					.withClient("demo-client-id")
					.secret("secret")
                    .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                    .redirectUris("http://demo.com/login/oauth2/code/demo","http://localhost/login/oauth2/code/demo","http://127.0.0.1/login/oauth2/code/demo")
                    .scopes("message:read", "message:write", "user:read")
					.accessTokenValiditySeconds(600_000_000)
				.and()
					.withClient("messaging-client")
					.secret("secret")
                    .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                    .redirectUris("http://demo.com/authorized","http://localhost/authorized","http://127.0.0.1/authorized")
                    .scopes("message.read", "message.write")
					.accessTokenValiditySeconds(600_000_000)
				.and()
					.withClient("shoulder")
					.secret("shoulder")
                    .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                    .redirectUris("http://demo.com/authorized","http://localhost/authorized","http://127.0.0.1/authorized")
                    .scopes("message.read", "message.write")
					.accessTokenValiditySeconds(600_000_000);
		//*/
        // @formatter:on
    }

    /**
     * 配置端点信息
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        // @formatter:off
        endpoints
                .authenticationManager(this.authenticationManager)
                .tokenStore(tokenStore());

        if (this.jwtEnabled) {
            endpoints
                    .accessTokenConverter(accessTokenConverter());
        }
        // @formatter:on
    }


    @Bean
    public TokenStore tokenStore() {
        TokenStore tokenStore;
        String type;
        if (this.jwtEnabled) {
            tokenStore = new JwtTokenStore(accessTokenConverter());
            type = "jwt";
        } else if (dataSource != null) {
            tokenStore = new JdbcTokenStore(dataSource);
            type = "jdbc";
        } else {
            tokenStore = new InMemoryTokenStore();
            type = "inMemory";
        }
        log.info("tokenStore type: " + type);
        return tokenStore;
    }

    //@Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(this.keyPair);

        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(new SubjectAttributeUserTokenConverter());
        converter.setAccessTokenConverter(accessTokenConverter);

        return converter;
    }


    /**
     * 传统授权服务器不支持用户参数的自定义名称，因此我们需要扩展默认值。默认情况下，它使用属性{@code user_name}，不过最好遵循 jwt 规范
     * <a target="_blank" href="https://tools.ietf.org/html/rfc7519">JWT Specification</a>.
     */
    class SubjectAttributeUserTokenConverter extends DefaultUserAuthenticationConverter {
        @Override
        public Map<String, ?> convertUserAuthentication(Authentication authentication) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("sub", authentication.getName());
            if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
                response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
            }
            return response;
        }
    }
}