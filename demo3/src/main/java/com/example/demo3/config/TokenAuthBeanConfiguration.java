package com.example.demo3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;

import java.util.HashMap;
import java.util.Map;

/**
 * 该类仅为 Token 认证提供
 * Session 方式认证需要注释 / 删除该类
 *
 * @author lym
 */
@Configuration
public class TokenAuthBeanConfiguration {

    /**
     * 自定义 token 认证中复用 spring security oauth server 的部分逻辑
     * 这里的信息会作为授权用户信息
     */
    @Bean
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

}