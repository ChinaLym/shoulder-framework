package com.example.demo4;

import org.shoulder.security.authentication.endpoint.JwkSetEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;

import java.util.HashMap;
import java.util.Map;

/**
 * shoulder-framework 实例工程
 * TIP：运行后（默认为本机8080端口），进入 controller 目录（已按照功能分类），点击方法上的超链接（IDE支持），即可快速查看效果
 *
 * @author lym
 */
@SpringBootApplication
public class Demo4Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo4Application.class, args);
    }

    @Autowired
    private JwkSetEndpoint jwkSetEndpoint;

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