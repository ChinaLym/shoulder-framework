package org.shoulder.validate.validator;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.validate.annotation.NoSpecialChar;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 特殊字符校验
 *
 * @author lym
 */
public class NoSpecialCharValidator implements ConstraintValidator<NoSpecialChar, String> {
    private String pattern = "";

    @Override
    public void initialize(NoSpecialChar constraintAnnotation) {
        pattern = constraintAnnotation.forbiddenPattern();
    }


    /**
     * @return true 不含特殊字符  false 含特殊字符
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = true;
        if (StringUtils.isNotEmpty(value)) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(value);
            result = !m.find();
        }
        return result;
    }
}
