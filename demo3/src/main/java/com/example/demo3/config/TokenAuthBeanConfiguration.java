package com.example.demo3.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;

import java.util.HashMap;
import java.util.Map;

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
    @ConditionalOnMissingBean(ClientDetailsService.class)
    public ClientDetailsService clientDetailsService() {
        InMemoryClientDetailsService service = new InMemoryClientDetailsService();

        Map<String, ClientDetails> clientDetailsStore = new HashMap<>();
        //String clientId, String resourceIds, String scopes, String grantTypes, String authorities
        BaseClientDetails details = new BaseClientDetails("shoulder", "oauth2-resource", "scopes", "grantTypes", "authorities");
        details.setClientSecret("shoulder");
        clientDetailsStore.put(details.getClientId(), details);

        service.setClientDetailsStore(clientDetailsStore);
        return service;
    }

    @Bean
    public DefaultTokenServices defaultTokenServices(InMemoryTokenStore tokenStore) {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore);
        return tokenServices;
    }

    @Bean
    public InMemoryTokenStore inMemoryTokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    public OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider(MyTokenIntrospector myTokenIntrospector) {
        return new OpaqueTokenAuthenticationProvider(myTokenIntrospector);
    }

}