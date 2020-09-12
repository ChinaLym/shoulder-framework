package org.shoulder.autoconfigure.security.code;

import org.shoulder.code.ValidateCodeFilter;
import org.shoulder.code.consts.ValidateCodeConsts;
import org.shoulder.code.processor.ValidateCodeProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnClass(ValidateCodeConsts.class)
@Configuration(
    proxyBeanMethods = false
)
@AutoConfigureAfter(ValidateCodeBeanConfig.class)
@ConditionalOnBean(value = {ValidateCodeProcessor.class})
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
