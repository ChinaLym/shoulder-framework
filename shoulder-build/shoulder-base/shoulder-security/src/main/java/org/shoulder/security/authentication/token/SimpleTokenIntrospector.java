package org.shoulder.security.authentication.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 简单 token 解析器
 * 从 tokenService 中，以 token 为 key 拿出来进行解析
 * 推荐自行覆盖实现：因为每个系统中用户信息不同、 token 也是自己发的，解析用户信息和验证 token 有效性与具体的设计相关
 *
 * @author lym
 */
public class SimpleTokenIntrospector implements OpaqueTokenIntrospector {

    private final ResourceServerTokenServices tokenServices;

    private final ClientDetailsService clientDetailsService;

    private String resourceId = "oauth2-resource";

    public SimpleTokenIntrospector(ResourceServerTokenServices tokenServices, ClientDetailsService clientDetailsService) {
        this.tokenServices = tokenServices;
        this.clientDetailsService = clientDetailsService;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2Authentication auth = tokenServices.loadAuthentication(token);
        if (auth == null) {
            throw new InvalidTokenException("Invalid token: " + token);
        }

        Collection<String> resourceIds = auth.getOAuth2Request().getResourceIds();
        if (resourceId != null && resourceIds != null && !resourceIds.isEmpty() && !resourceIds.contains(resourceId)) {
            throw new OAuth2AccessDeniedException("Invalid token does not contain resource id (" + resourceId + ")");
        }
        checkClientDetails(auth);
        Map<String, Object> attributes = new HashMap<>();
        Authentication userAuthentication = auth.getUserAuthentication();
        // todo 获取 userAuthentication 所有字段，放入 attributes
        attributes.put("name", auth.getUserAuthentication().getName());
        return new DefaultOAuth2User(auth.getAuthorities(), attributes, "name");
    }


    private void checkClientDetails(OAuth2Authentication auth) {
        if (clientDetailsService != null) {
            ClientDetails client;
            try {
                client = clientDetailsService.loadClientByClientId(auth.getOAuth2Request().getClientId());
            } catch (ClientRegistrationException e) {
                throw new OAuth2AccessDeniedException("Invalid token contains invalid client id");
            }
            Set<String> allowed = client.getScope();
            for (String scope : auth.getOAuth2Request().getScope()) {
                if (!allowed.contains(scope)) {
                    throw new OAuth2AccessDeniedException(
                        "Invalid token contains disallowed scope (" + scope + ") for this client");
                }
            }
        }
    }


}
