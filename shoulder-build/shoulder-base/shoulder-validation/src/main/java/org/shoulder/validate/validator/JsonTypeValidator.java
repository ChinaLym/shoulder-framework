package org.shoulder.validate.validator;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.validate.annotation.JsonType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;

/**
 * {@link JsonType} 注解校验处理，支持数组
 *
 * @author lym
 * @see JsonType
 */
public class JsonTypeValidator implements ConstraintValidator<JsonType, String> {

    @Override
    public boolean isValid(String input, ConstraintValidatorContext context) {
        if (input == null || input.length() == 0) {
            return true;
        }
        try {
            JsonUtils.toObject(input, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}


