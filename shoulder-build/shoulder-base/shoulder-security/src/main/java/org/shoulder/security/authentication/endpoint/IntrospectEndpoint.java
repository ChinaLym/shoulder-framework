package org.shoulder.security.authentication.endpoint;

import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于校验token是否有效以及返回该token的信息。
 * <p>
 * 传统授权服务器（spring-security-oauth2）不支持 /introspect。本类特地增加该端点，以更好地支持校验回调。
 *
 * TODO 废弃与 spring security oauth 项目相关的类，改为替代品 {@link OAuth2ResourceServerConfigurer}
 * 校验接口用处：与 {@link JwkSetEndpoint}类似，提供了远程校验 token 是否有效，不需要客户端理解：
 * @author lym
 */
@Controller
public class IntrospectEndpoint {

    private TokenStore tokenStore;

    public IntrospectEndpoint(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    /**
     * 校验accessToken是否有效以及对应信息
     *
     * @param token accessToken
     * @return accessToken的信息
     */
    @PostMapping("/token/introspect")
    @ResponseBody
    public Map<String, Object> introspect(@RequestParam("token") String token) {
        // 尝试从 accessToken 缓存中取，如果没有，则返回失效
        OAuth2AccessToken accessToken = this.tokenStore.readAccessToken(token);
        Map<String, Object> attributes = new HashMap<>(4);
        // 判断是否有效
        boolean invalid = accessToken == null || accessToken.isExpired();
        boolean active = !invalid;

        attributes.put("active", active);
        // 若失效，则提前返回
        if (invalid) {
            return attributes;
        }
        // 获取与 accessToken 对应的认证信息填充返回
        OAuth2Authentication authentication = this.tokenStore.readAuthentication(token);

        attributes.put("exp", accessToken.getExpiration().getTime());
        attributes.put("scope", String.join(" ", accessToken.getScope()));
        attributes.put("sub", authentication.getName());

        return attributes;
    }
}
