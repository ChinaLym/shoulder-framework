package org.shoulder.security.authentication;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.security.SecurityConst;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    private final AuthenticationFailureHandler authenticationFailureHandler;

    private final LogoutSuccessHandler logoutSuccessHandler;

    private final List<LogoutHandler> logoutHandlers;

    private String loginPageUrl;

    public FormAuthenticationSecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler,
                                            AuthenticationFailureHandler authenticationFailureHandler,
                                            LogoutSuccessHandler logoutSuccessHandler,
                                            @Nullable List<LogoutHandler> logoutHandlers) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.logoutHandlers = logoutHandlers;
        // 默认跳转到待认证处理器
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        if (loginPageUrl == null) {
            // throw new IllegalStateException("please invoke setLoginPageUrl before configure.");
            // fallback to default
            loginPageUrl = SecurityConst.URL_REQUIRE_AUTHENTICATION;
        }
        // @formatter:off
        http
            .formLogin(formLoginConfig ->
                    formLoginConfig.loginPage(loginPageUrl)
                    .loginProcessingUrl(SecurityConst.URL_AUTHENTICATION_FORM)
                    .successHandler(authenticationSuccessHandler)
                    .failureHandler(authenticationFailureHandler));


        // @formatter:on

        http.logout(
                logoutConfiguration -> {
                    logoutConfiguration.logoutSuccessHandler(logoutSuccessHandler);
                    if (CollectionUtils.isNotEmpty(logoutHandlers)) {
                        for (LogoutHandler logoutHandler : logoutHandlers) {
                            logoutConfiguration.addLogoutHandler(logoutHandler);
                        }
                    }
                });


    }

    public void setLoginPageUrl(String loginPageUrl) {
        this.loginPageUrl = loginPageUrl;
    }
}
