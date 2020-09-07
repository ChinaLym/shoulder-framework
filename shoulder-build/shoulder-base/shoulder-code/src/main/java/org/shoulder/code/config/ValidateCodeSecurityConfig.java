package org.shoulder.code.config;

import org.shoulder.code.ValidateCodeFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * 装配验证码过滤器
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
public class ValidateCodeSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private ValidateCodeFilter validateCodeFilter;

    public ValidateCodeSecurityConfig(ValidateCodeFilter validateCodeFilter) {
        this.validateCodeFilter = validateCodeFilter;
    }

    @Override
    public void configure(HttpSecurity http) {
        // 验证码过滤器加在认证处理器之前，以支持登录认证请求也可以校验验证码
        http.addFilterBefore(validateCodeFilter, AbstractPreAuthenticatedProcessingFilter.class);
    }

}
