package org.shoulder.autoconfigure.validate;

import org.shoulder.validate.support.extract.ConstraintExtract;
import org.shoulder.validate.support.extract.DefaultConstraintExtractImpl;
import org.shoulder.validate.support.mateconstraint.ConstraintConverter;
import org.shoulder.validate.support.mateconstraint.impl.*;
import org.shoulder.web.validate.ValidateRuleEndPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态校验规则
 *
 * @author lym
 */
@AutoConfiguration
@ConditionalOnClass(ConstraintExtract.class)
@ConditionalOnProperty(value = "shoulder.validate.auto-rule.enable", havingValue = "true", matchIfMissing = true)
public class DynamicValidationRuleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConstraintExtract constraintExtract(Validator validator, List<ConstraintConverter> constraintConverters) {
        List<ConstraintConverter> constraintConverterList = new ArrayList<>(constraintConverters.size() + 5);
        constraintConverterList.add(new MaxMinConstraintConverter());
        constraintConverterList.add(new NotNullConstraintConverter());
        constraintConverterList.add(new RangeConstraintConverter());
        constraintConverterList.add(new RegexConstraintConverter());
        constraintConverterList.add(new OtherConstraintConverter());
        return new DefaultConstraintExtractImpl(validator, constraintConverterList);
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidateRuleEndPoint validateRuleEndPoint(ConstraintExtract constraintExtract,
                                                     RequestMappingHandlerMapping requestMappingHandlerMapping,
                                                     @Value(ValidateRuleEndPoint.VALIDATION_RULE_URL_VALUE_EXPRESSION)
                                                             String validationRuleUrl) {
        return new ValidateRuleEndPoint(constraintExtract, requestMappingHandlerMapping, validationRuleUrl);
    }

}
