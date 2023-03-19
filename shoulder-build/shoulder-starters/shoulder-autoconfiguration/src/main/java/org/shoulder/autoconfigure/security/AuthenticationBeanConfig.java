package org.shoulder.autoconfigure.security;

import org.shoulder.security.SecurityConst;
import org.shoulder.security.authentication.FormAuthenticationSecurityConfig;
import org.shoulder.security.authentication.sms.PhoneNumAuthenticateService;
import org.shoulder.security.authentication.sms.PhoneNumAuthenticationSecurityConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 认证相关基本 bean 配置（用户名密码登录、手机号登录）
 *
 * @author lym
 */
@AutoConfiguration(after = AuthenticationHandlerConfig.class)
@EnableConfigurationProperties(AuthenticationProperties.class)
@ConditionalOnClass(SecurityConst.class)
public class AuthenticationBeanConfig {

    /**
     * 密码处理器，默认使用 spring security 推荐的 BCryptPasswordEncoder
     * 若需替换，推荐使用单向加密，如 SHA256
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用户名、密码认证(表单登录)配置
     */
    @Bean
    @ConditionalOnBean(UserDetailsService.class)
    public FormAuthenticationSecurityConfig formAuthenticationConfig(@Nullable AuthenticationSuccessHandler authenticationSuccessHandler,
                                                                     @Nullable AuthenticationFailureHandler authenticationFailureHandler,
                                                                     @Nullable LogoutSuccessHandler logoutSuccessHandler,
                                                                     @Nullable List<LogoutHandler> logoutHandlers) {
        return new FormAuthenticationSecurityConfig(authenticationSuccessHandler, authenticationFailureHandler,
            logoutSuccessHandler, logoutHandlers);
    }

    /**
     * 手机短信验证码认证(短信验证码登录)配置
     * 要激活必须先存在 PhoneNumAuthenticateService 类
     */
    @Bean
    @ConditionalOnBean(PhoneNumAuthenticateService.class)
    public PhoneNumAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig(@Nullable AuthenticationSuccessHandler authenticationSuccessHandler,
                                                                                    @Nullable AuthenticationFailureHandler authenticationFailureHandler,
                                                                                    PhoneNumAuthenticateService userDetailsService) {

        return new PhoneNumAuthenticationSecurityConfig(authenticationSuccessHandler, authenticationFailureHandler, userDetailsService);
    }

}
