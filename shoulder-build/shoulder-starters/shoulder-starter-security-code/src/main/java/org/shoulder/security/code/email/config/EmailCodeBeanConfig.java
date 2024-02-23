package org.shoulder.security.code.email.config;

import org.shoulder.autoconfigure.security.code.ValidateCodeBeanConfig;
import org.shoulder.code.store.ValidateCodeStore;
import org.shoulder.security.code.email.EmailCodeGenerator;
import org.shoulder.security.code.email.EmailCodeProcessor;
import org.shoulder.security.code.email.EmailCodeSender;
import org.shoulder.security.code.email.MockEmailCodeSender;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * email验证码自动配置
 *
 * @author lym
 */
@AutoConfiguration(before = ValidateCodeBeanConfig.class)
@EnableConfigurationProperties(EmailCodeProperties.class)
public class EmailCodeBeanConfig {

    @Bean
    @ConditionalOnMissingBean(EmailCodeGenerator.class)
    public EmailCodeGenerator emailCodeGenerator(EmailCodeProperties emailCodeProperties) {
        return new EmailCodeGenerator(emailCodeProperties);
    }


    @Bean
    @ConditionalOnMissingBean(EmailCodeProcessor.class)
    public EmailCodeProcessor emailCodeProcessor(EmailCodeProperties emailCodeProperties,
                                                 EmailCodeGenerator emailCodeGenerator,
                                                 ValidateCodeStore validateCodeStore,
                                                 EmailCodeSender emailCodeSender) {

        return new EmailCodeProcessor(emailCodeProperties, emailCodeGenerator, validateCodeStore, emailCodeSender);

    }

    /**
     * mock的仅在 dev 生效
     *
     * @return bean
     */
    @Bean
    @Profile("dev")
    @ConditionalOnMissingBean(EmailCodeSender.class)
    public EmailCodeSender emailCodeSender() {
        return new MockEmailCodeSender();
    }

}
