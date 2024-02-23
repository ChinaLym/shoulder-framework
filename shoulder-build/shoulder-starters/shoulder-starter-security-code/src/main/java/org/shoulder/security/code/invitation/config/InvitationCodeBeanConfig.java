package org.shoulder.security.code.invitation.config;

import org.shoulder.autoconfigure.security.code.ValidateCodeBeanConfig;
import org.shoulder.code.store.ValidateCodeStore;
import org.shoulder.security.code.invitation.InvitationCodeGenerator;
import org.shoulder.security.code.invitation.InvitationCodeProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 邀请制-验证码自动配置
 *
 * @author lym
 */
@AutoConfiguration(before = ValidateCodeBeanConfig.class)
@EnableConfigurationProperties(InvitationCodeProperties.class)
public class InvitationCodeBeanConfig {

    @Bean
    @ConditionalOnMissingBean(InvitationCodeGenerator.class)
    public InvitationCodeGenerator invitationCodeGenerator(InvitationCodeProperties invitationCodeProperties) {
        return new InvitationCodeGenerator(invitationCodeProperties);
    }

    @Bean
    @ConditionalOnMissingBean(InvitationCodeProcessor.class)
    public InvitationCodeProcessor invitationCodeProcessor(InvitationCodeProperties invitationCodeProperties,
                                                           InvitationCodeGenerator codeGenerator,
                                                           ValidateCodeStore validateCodeStore) {

        return new InvitationCodeProcessor(invitationCodeProperties, codeGenerator, validateCodeStore);

    }

}
