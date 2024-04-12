package org.shoulder.validate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.validate.annotation.Enum;
import org.slf4j.Logger;

/**
 * {@link Enum} 注解校验处理
 *
 * @author lym
 */
public class EnumValidator implements ConstraintValidator<Enum, CharSequence> {

    private static final Logger log = ShoulderLoggers.SHOULDER_DEFAULT;

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
        if (value == null) {
            return canBeNull;
        }
        for (String allowValue : allowValues) {
            if (value.equals(allowValue)) {
                return true;
            }
        }
        return false;
    }

}


