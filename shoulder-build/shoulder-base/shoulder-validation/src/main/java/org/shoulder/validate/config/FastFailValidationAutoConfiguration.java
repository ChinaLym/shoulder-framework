package org.shoulder.validate.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * 快速失败异常校验配置
 *
 * @author lym
 */
@AutoConfigureBefore(ValidationAutoConfiguration.class)
public class FastFailValidationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Validator validator() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
            .configure()
            //failFast的意思只要出现校验失败的情况，就立即结束校验，不再进行后续的校验。
            .failFast(true)
            .buildValidatorFactory();

        return validatorFactory.getValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public MethodValidationPostProcessor methodValidationPostProcessor(Validator validator) {
        MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(validator);
        return methodValidationPostProcessor;
    }
}
