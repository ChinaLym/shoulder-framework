package org.shoulder.security.authentication.sms;

import lombok.extern.shoulder.SLog;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 短信验证码登录相关配置
 * 扩展 spring security 认证方式，提供短信验证码认证
 *
 * @author lym
 */
@SLog
public class PhoneNumAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private AuthenticationSuccessHandler authenticationSuccessHandler;

    private AuthenticationFailureHandler authenticationFailureHandler;

    private PhoneNumAuthenticateService phoneNumAuthenticateService;

    public PhoneNumAuthenticationSecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler,
                                                AuthenticationFailureHandler authenticationFailureHandler,
                                                PhoneNumAuthenticateService phoneNumAuthenticateService) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.phoneNumAuthenticateService = phoneNumAuthenticateService;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {

        if (phoneNumAuthenticateService == null) {
            // 为空则不装配
            log.debug("userAuthenticateService is null, ignore configure");
            return;
        }

        PhoneNumAuthenticationFilter phoneNumAuthenticationFilter = new PhoneNumAuthenticationFilter();
        phoneNumAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        phoneNumAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        phoneNumAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        // 添加 短信认证
        PhoneNumAuthenticationProvider phoneNumAuthenticationProvider = new PhoneNumAuthenticationProvider(phoneNumAuthenticateService);

        http.authenticationProvider(phoneNumAuthenticationProvider)
            .addFilterAfter(phoneNumAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
