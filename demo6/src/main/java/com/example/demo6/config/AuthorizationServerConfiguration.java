package com.example.demo6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 测试扩展 spring jwt 信息 （可选）
 *
 * @author lym
 */
@Configuration
public class AuthorizationServerConfiguration {

    /**
     * 测试扩展 spring jwt 信息
     */
    @Bean
    public UserAuthenticationConverter userAuthenticationConverter() {
        return new SubjectAttributeUserTokenConverter();
    }

    /**
     * 传统授权服务器不支持用户参数的自定义名称，因此我们需要扩展默认值。默认情况下，它使用属性{@code user_name}，不过最好遵循 jwt 规范
     * <a target="_blank" href="https://tools.ietf.org/html/rfc7519">JWT Specification</a>.
     */
    public static class SubjectAttributeUserTokenConverter extends DefaultUserAuthenticationConverter {
        @Override
        public Map<String, ?> convertUserAuthentication(Authentication authentication) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("sub", authentication.getName());
            if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
                response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
            }
            return response;
        }
    }
}