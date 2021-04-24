package com.example.demo3.config;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.security.authentication.AuthenticationType;
import org.shoulder.security.authentication.handler.json.FormTokenAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 该类仅为 Token 认证提供客户端信息解析器、Session 方式认证可删除该类
 * 本 demo 中，未使用 ClientDetails 作为颁发 token 的区别（只有一个账户）
 *
 * @author lym
 */
@Configuration
@ConditionalOnAuthType(type = AuthenticationType.TOKEN)
public class TokenAuthBeanConfiguration {

    public static final BaseClientDetails TEST_CLIENT_DETAIL;

    static {
        TEST_CLIENT_DETAIL = new BaseClientDetails(
                "shoulder",
                // 资源标识，可以使用 appId（服务标识）、或具体资源标识，如用户信息、用户组信息、动态
                "oauth2-resource",
                // 客户端申请的权限范围,可选值包括read,write,trust;若有多个权限范围用逗号(,)分隔
                "scopes",
                // 指定客户端支持的grant_type,可选值包括authorization_code,password,refresh_token,implicit,client_credentials, 若支持多个grant_type用逗号(,)
                "grantTypes",
                // @EnableGlobalMethodSecurity(prePostEnabled = true)启用方法级权限控制，方法上@PreAuthorize("#oauth2.hasScope('read')")
                "authorities");

        TEST_CLIENT_DETAIL.setClientSecret("shoulder");
    }

    @Bean
    public ClientDetailsService clientDetailsService() {
        // 只有一个账户
        InMemoryClientDetailsService service = new InMemoryClientDetailsService();
        Map<String, ClientDetails> clientDetailsStore = new HashMap<>();
        clientDetailsStore.put(TEST_CLIENT_DETAIL.getClientId(), TEST_CLIENT_DETAIL);
        service.setClientDetailsStore(clientDetailsStore);
        return service;
    }

    /**
     * token 认证成功处理器
     */
    @Bean
    public AuthenticationSuccessHandler tokenAuthenticationSuccessHandler(ClientDetailsService clientDetailsService, AuthorizationServerTokenServices authorizationServerTokenServices) {
        return new FormTokenAuthenticationSuccessHandler("username", "password", clientDetailsService, authorizationServerTokenServices);
    }

}
