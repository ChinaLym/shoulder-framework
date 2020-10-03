package com.example.demo6.config;

import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.HashMapKeyPairCache;
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
import org.springframework.security.core.userdetails.UserDetailsService;
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
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
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

    @Autowired(required = false)
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    TokenStore tokenStore;

    /**
     * 默认使用 jwt
     */
    @Autowired
    @Value("${security.oauth2.authorizationserver.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Autowired//(required = false)
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

        if (dataSource != null) {
            clients.jdbc(dataSource);
            return;
        }

        int hours2 = (int) Duration.ofHours(2).toSeconds();
        int day60 = (int) Duration.ofDays(60).toSeconds();
        // @formatter:off
        //*
		// 创建默认的 clientDetail
		clients.inMemory()
				.withClient("reader")
					.secret("secret")
					.authorizedGrantTypes("password")
					.scopes("message:read")
					.accessTokenValiditySeconds(hours2)
                    .refreshTokenValiditySeconds(day60)
				.and()
					.withClient("writer")
					.secret("secret")
					.accessTokenValiditySeconds(hours2)
                    .refreshTokenValiditySeconds(day60)
					.authorizedGrantTypes("password")
					.scopes("message:write")
				.and()
					.withClient("noscopes")
					.secret("secret")
					.accessTokenValiditySeconds(hours2)
                    .refreshTokenValiditySeconds(day60)
					.authorizedGrantTypes("password")
					.scopes("none")
				.and()
					.withClient("demo-client-id")
					.secret("secret")
					.accessTokenValiditySeconds(hours2)
                    .refreshTokenValiditySeconds(day60)
                    .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                    .redirectUris("http://demo.com/login/oauth2/code/demo","http://localhost/login/oauth2/code/demo","http://127.0.0.1/login/oauth2/code/demo")
                    .scopes("message:read", "message:write", "user:read")
				.and()
					.withClient("messaging-client")
					.secret("secret")
					.accessTokenValiditySeconds(hours2)
                    .refreshTokenValiditySeconds(day60)
                    .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                    .redirectUris("http://demo.com/authorized","http://localhost/authorized","http://127.0.0.1/authorized")
                    .scopes("message.read", "message.write")
				.and()
					.withClient("shoulder")
					.secret("shoulder")
					.accessTokenValiditySeconds(hours2)
                    .refreshTokenValiditySeconds(day60)
                    .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                    .redirectUris("http://demo.com/authorized","http://localhost/authorized","http://127.0.0.1/authorized")
                    .scopes("message.read", "message.write")

                ;

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
                .tokenStore(tokenStore)
                .userDetailsService(userDetailsService)
        ;

        if (this.jwtAccessTokenConverter != null) {
            endpoints
                    .accessTokenConverter(jwtAccessTokenConverter);
        }
        // @formatter:on
    }

    /**
     * 决定认证服务器将 token 存到哪
     */
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        TokenStore tokenStore;
        String type;
        if (this.jwtEnabled) {
            tokenStore = new JwtTokenStore(jwtAccessTokenConverter);
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


    @Bean("jwkKeyPair")
    public KeyPair buildKeyPair() {
        try {
            String privateExponent = "3851612021791312596791631935569878540203393691253311342052463788814433805390794604753109719790052408607029530149004451377846406736413270923596916756321977922303381344613407820854322190592787335193581632323728135479679928871596911841005827348430783250026013354350760878678723915119966019947072651782000702927096735228356171563532131162414366310012554312756036441054404004920678199077822575051043273088621405687950081861819700809912238863867947415641838115425624808671834312114785499017269379478439158796130804789241476050832773822038351367878951389438751088021113551495469440016698505614123035099067172660197922333993";
            String modulus = "18044398961479537755088511127417480155072543594514852056908450877656126120801808993616738273349107491806340290040410660515399239279742407357192875363433659810851147557504389760192273458065587503508596714389889971758652047927503525007076910925306186421971180013159326306810174367375596043267660331677530921991343349336096643043840224352451615452251387611820750171352353189973315443889352557807329336576421211370350554195530374360110583327093711721857129170040527236951522127488980970085401773781530555922385755722534685479501240842392531455355164896023070459024737908929308707435474197069199421373363801477026083786683";
            String exponent = "66666";

            RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
            RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(
                    new BigInteger(modulus), new BigInteger(privateExponent)
            );
            KeyFactory factory = KeyFactory.getInstance("RSA");

            //return new KeyPair(factory.generatePublic(publicSpec), factory.generatePrivate(privateSpec));

            KeyPairCache cache = new HashMapKeyPairCache();
            DefaultAsymmetricCryptoProcessor processor = DefaultAsymmetricCryptoProcessor.rsa2048(cache);
            processor.buildKeyPair("jwk");
            return cache.get("jwk").getOriginKeyPair();

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * 用于将 jwt 解码，转为实际 token 与其对应信息的
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter(KeyPair keyPair) {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyPair);

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