package org.shoulder.validate.validator;

import org.shoulder.validate.annotation.Enum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@link Enum} 注解校验处理
 * @author lym
 */
public class EnumValidator implements ConstraintValidator<Enum, CharSequence> {

    private static final Logger log = LoggerFactory.getLogger(EnumValidator.class);

    private String[] allowValues;

    private boolean canBeNull = false;

    @Override
    public void initialize(Enum constraintAnnotation) {
        allowValues = constraintAnnotation.enums();
        for (String allowValue : allowValues) {
            if (allowValue == null) {
                canBeNull = true;
                break;
            }
        }

    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if(value == null){
            return canBeNull;
        }
        for (String allowValue : allowValues){
            if(value.equals(allowValue)){
                return true;
            }
        }
        return false;
    }

}


