package org.shoulder.autoconfigure.security;

import org.shoulder.autoconfigure.condition.ConditionalOnAuthType;
import org.shoulder.autoconfigure.security.browser.BrowserSessionAuthProperties;
import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.AuthenticationType;
import org.shoulder.security.authentication.handler.json.*;
import org.shoulder.security.authentication.handler.url.RedirectAuthenticationFailureHandler;
import org.shoulder.security.authentication.handler.url.RedirectAuthenticationSuccessHandler;
import org.shoulder.security.authentication.handler.url.RedirectLogoutSuccessHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * 认证相关基本 bean 配置（用户名密码登录、手机号登录）
 *
 * @author lym
 */
@AutoConfiguration
@EnableConfigurationProperties(AuthenticationProperties.class)
@ConditionalOnClass(SecurityConst.class)
public class AuthenticationHandlerConfig {

    /**
     * 认证响应格式为 session 时生效
     */
    @EnableConfigurationProperties(BrowserSessionAuthProperties.class)
    @ConditionalOnAuthType(type = AuthenticationType.SESSION)
    @AutoConfiguration
    @ConditionalOnProperty(name = "shoulder.security.auth.responseType", havingValue = "redirect", matchIfMissing = true)
    static class RedirectResponseHandlerConfiguration {

        private final BrowserSessionAuthProperties browserSessionAuthProperties;

        RedirectResponseHandlerConfiguration(BrowserSessionAuthProperties browserSessionAuthProperties) {
            this.browserSessionAuthProperties = browserSessionAuthProperties;
        }

        @Bean
        @ConditionalOnMissingBean(LogoutSuccessHandler.class)
        public LogoutSuccessHandler redirectLogoutSuccessHandler() {
            return new RedirectLogoutSuccessHandler(browserSessionAuthProperties.getSignOutSuccessUrl());
        }

        @Bean
        @ConditionalOnMissingBean(AuthenticationSuccessHandler.class)
        public AuthenticationSuccessHandler redirectAuthenticationSuccessHandler() {
            return new RedirectAuthenticationSuccessHandler(
                browserSessionAuthProperties.getSignInSuccessUrl());
        }

        @Bean
        @ConditionalOnMissingBean(AuthenticationFailureHandler.class)
        public AuthenticationFailureHandler redirectAuthenticationFailureHandler() {
            return new RedirectAuthenticationFailureHandler(null);
        }
    }

    /**
     * 认证响应格式为 json 时生效
     */
    @AutoConfiguration
    @ConditionalOnProperty(name = "shoulder.security.auth.responseType", havingValue = "json")
    static class JsonResponseHandlerConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public LogoutSuccessHandler jsonLogoutSuccessHandler() {
            return new JsonLogoutSuccessHandler();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnAuthType(type = AuthenticationType.SESSION)
        public AuthenticationSuccessHandler jsonAuthenticationSuccessHandler() {
            return new JsonAuthenticationSuccessHandler();
        }

        @Bean
        @ConditionalOnMissingBean
        public AuthenticationFailureHandler jsonAuthenticationFailureHandler() {
            return new JsonAuthenticationFailureHandler();
        }

        @Bean
        @ConditionalOnMissingBean
        public Restful401AuthenticationEntryPoint restful401AuthenticationEntryPoint() {
            return new Restful401AuthenticationEntryPoint();
        }

        @Bean
        @ConditionalOnMissingBean
        public Restful403AccessDeniedHandler restful403AccessDeniedHandler() {
            return new Restful403AccessDeniedHandler();
        }
    }

}
