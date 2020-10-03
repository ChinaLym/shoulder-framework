package org.shoulder.security.authentication;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.security.SecurityConst;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.List;

/**
 * 用户名、密码认证(表单登录)配置
 * 使用 spring security 默认提供的用户名密码认证
 *
 * @author lym
 */
public class FormAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private AuthenticationSuccessHandler authenticationSuccessHandler;

    private AuthenticationFailureHandler authenticationFailureHandler;

    private LogoutSuccessHandler logoutSuccessHandler;

    private List<LogoutHandler> logoutHandlers;

    public FormAuthenticationSecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler,
                                            AuthenticationFailureHandler authenticationFailureHandler,
                                            LogoutSuccessHandler logoutSuccessHandler,
                                            @Nullable List<LogoutHandler> logoutHandlers) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.logoutHandlers = logoutHandlers;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .formLogin()
            //todo loginPage url
                //.loginPage(SecurityConst.URL_REQUIRE_AUTHENTICATION)
                .loginProcessingUrl(SecurityConst.URL_AUTHENTICATION_FORM)
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler);

        // @formatter:on

        LogoutConfigurer<HttpSecurity> logoutConfigurer = http.logout().logoutSuccessHandler(logoutSuccessHandler);

        if (CollectionUtils.isNotEmpty(logoutHandlers)) {
            for (LogoutHandler logoutHandler : logoutHandlers) {
                logoutConfigurer.addLogoutHandler(logoutHandler);
            }
        }
    }

}
