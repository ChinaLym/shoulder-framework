package com.example.demo4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * 该类仅为 Token 认证提供
 * <p>
 * Session 方式认证需要注释 / 删除该类
 *
 * @author lym
 */
@Configuration
public class TokenAuthBeanConfiguration {


    @Bean
    public DefaultTokenServices defaultTokenServices(TokenStore tokenStore) {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore);
        return tokenServices;
    }


}