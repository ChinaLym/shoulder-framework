package com.example.demo6.config;

import org.springframework.context.annotation.Configuration;

/**
 * 注意，token 认证不需要开启 @EnableResourceServer @EnableAuthorizationServer
 * 因为仅仅是借用了其生成 oauth2 token的流程，不必激活其资源/认证服务器的相关配置
 *
 * @author lym
 */
@Configuration
public class TokenAuthConfig {

    /*@Bean
    @ConditionalOnMissingBean(ClientDetailsService.class)
    public ClientDetailsService clientDetailsService() {
        InMemoryClientDetailsService service = new InMemoryClientDetailsService();

        Map<String, ClientDetails> clientDetailsStore = new HashMap<>();
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
    }*/

}
