package com.example.demo3.config;


/**
 * 该类仅为 Token 认证提供
 *
 * Session 方式认证需要注释 / 删除该类
 *
 * @author lym
 */
/*
@Configuration
public class TokenAuthBeanConfiguration {

    @Bean
    @ConditionalOnMissingBean(ClientDetailsService.class)
    public ClientDetailsService clientDetailsService() {
        InMemoryClientDetailsService service = new InMemoryClientDetailsService();

        Map<String, ClientDetails> clientDetailsStore = new HashMap<>();
        //String clientId, String resourceIds, String scopes, String grantTypes, String authorities
        BaseClientDetails details = new BaseClientDetails("shoulder", "resourceIds", "resourceIds", "resourceIds", "resourceIds");
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


}*/
