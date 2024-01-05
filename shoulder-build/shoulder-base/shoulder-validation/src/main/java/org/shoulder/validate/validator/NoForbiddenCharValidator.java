package org.shoulder.validate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.shoulder.validate.annotation.NoForbiddenChar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 特殊字符校验
 *
 * @author lym
 */
public class NoForbiddenCharValidator implements ConstraintValidator<NoForbiddenChar, CharSequence> {
    private Pattern forbiddenPattern;


    @Override
    public void initialize(NoForbiddenChar constraintAnnotation) {
        String forbiddenPatternStr = constraintAnnotation.forbiddenPattern();
        this.forbiddenPattern = Pattern.compile(forbiddenPatternStr);
    }


    /**
     * @return true 不含特殊字符  false 含特殊字符
     */
    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        boolean result = true;
        if (StringUtils.isNotEmpty(value)) {
            Matcher m = forbiddenPattern.matcher(value);
            result = !m.find();
        }
        return result;
    }
}
