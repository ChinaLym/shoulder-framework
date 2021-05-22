package org.shoulder.security.code.sms.config;

import org.shoulder.autoconfigure.security.code.ValidateCodeBeanConfig;
import org.shoulder.code.store.ValidateCodeStore;
import org.shoulder.security.code.sms.MockSmsCodeSender;
import org.shoulder.security.code.sms.SmsCodeGenerator;
import org.shoulder.security.code.sms.SmsCodeProcessor;
import org.shoulder.security.code.sms.SmsCodeSender;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 短信验证码自动配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ValidateCodeBeanConfig.class)
@EnableConfigurationProperties(SmsCodeProperties.class)
public class SmsCodeBeanConfig {

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

    /**
     * mock的仅在 dev 生效
     *
     * @return bean
     */
    @Bean
    @Profile("dev")
    @ConditionalOnMissingBean(SmsCodeSender.class)
    public SmsCodeSender smsCodeSender() {
        return new MockSmsCodeSender();
    }

}
