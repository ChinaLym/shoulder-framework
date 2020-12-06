package com.example.demo3.config;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.security.authentication.AuthenticationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;

import java.util.HashMap;
import java.util.Map;

// 该类仅为 Token 认证提供客户端信息解析器、Session 方式认证需要注释 / 删除该类
@Configuration
@ConditionalOnAuthType(type = AuthenticationType.TOKEN)
public class TokenAuthBeanConfiguration {

    // 自定义 token 认证中复用 spring security oauth server 的部分逻辑,这里的信息会作为授权用户信息


    @Bean
    public ClientDetailsService clientDetailsService() {
        InMemoryClientDetailsService service = new InMemoryClientDetailsService();

        Map<String, ClientDetails> clientDetailsStore = new HashMap<>();
        //String clientId, String resourceIds, String scopes, String grantTypes, String authorities
        BaseClientDetails details = new BaseClientDetails(
                "shoulder",
                "oauth2-resource", // 资源标识，可以使用 appId（服务标识）、或具体资源标识，如用户信息、用户组信息、动态
                "scopes", // 客户端申请的权限范围,可选值包括read,write,trust;若有多个权限范围用逗号(,)分隔
                // @EnableGlobalMethodSecurity(prePostEnabled = true)启用方法级权限控制，方法上@PreAuthorize("#oauth2.hasScope('read')")
                "grantTypes",// 指定客户端支持的grant_type,可选值包括authorization_code,password,refresh_token,implicit,client_credentials, 若支持多个grant_type用逗号(,)
                "authorities");
        details.setClientSecret("shoulder");
        clientDetailsStore.put(details.getClientId(), details);

        service.setClientDetailsStore(clientDetailsStore);
        return service;
    }

}
