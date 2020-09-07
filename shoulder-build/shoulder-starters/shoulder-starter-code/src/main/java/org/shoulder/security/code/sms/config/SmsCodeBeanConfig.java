package org.shoulder.security.code.sms.config;

import org.shoulder.code.config.ValidateCodeBeanConfig;
import org.shoulder.code.store.ValidateCodeStore;
import org.shoulder.security.authentication.sms.SmsCodeAuthenticationSecurityConfig;
import org.shoulder.security.code.sms.*;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 短信验证码自动配置
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
@AutoConfigureBefore(ValidateCodeBeanConfig.class)
@EnableConfigurationProperties(SmsCodeProperties.class)
public class SmsCodeBeanConfig {

    /**
     * 手机短信验证码认证(短信验证码登录)配置
     */
    @Bean
    public SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig(@Nullable AuthenticationSuccessHandler authenticationSuccessHandler,
                                                                                   @Nullable AuthenticationFailureHandler authenticationFailureHandler,
                                                                                   UserDetailsService userDetailsService) {

        return new SmsCodeAuthenticationSecurityConfig(authenticationSuccessHandler, authenticationFailureHandler, userDetailsService);
    }

    @Bean
    @ConditionalOnMissingBean(SmsCodeGenerator.class)
    public SmsCodeGenerator smsCodeGenerator(SmsCodeProperties smsCodeProperties) {
        return new SmsCodeGenerator(smsCodeProperties);
    }


    @Bean
    @ConditionalOnMissingBean(SmsCodeProcessor.class)
    public SmsCodeProcessor smsCodeProcessor(SmsCodeProperties smsCodeProperties,
                                             SmsCodeGenerator smsCodeGenerator,
                                             ValidateCodeStore validateCodeStore,
                                             SmsCodeSender smsCodeSender) {

        return new SmsCodeProcessor(smsCodeProperties, smsCodeGenerator, validateCodeStore, smsCodeSender);

    }

    @Bean
    @ConditionalOnMissingBean(SmsCodeSender.class)
    public SmsCodeSender smsCodeSender() {
        return new DefaultSmsCodeSender();
    }

}
