package com.example.demo3.config;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class MyTokenIntrospector implements OpaqueTokenIntrospector {

    @Autowired
    private ResourceServerTokenServices tokenServices;

    @Autowired
    private ClientDetailsService clientDetailsService;

    private String resourceId = "oauth2-resource";

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
