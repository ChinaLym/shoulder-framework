package com.example.demo3.token;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;

@EnableAuthorizationServer
@TestConfiguration
public class TestBeanConfiguration {

    @Bean
    @ConditionalOnMissingBean(ClientDetailsService.class)
    public ClientDetailsService clientDetailsService() {

        return new InMemoryClientDetailsService();
    }
}
