package org.shoulder.validate.validator;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.validate.annotation.NoForbiddenChar;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 特殊字符校验
 *
 * @author lym
 */
public class OnlyAllowCharValidator implements ConstraintValidator<NoForbiddenChar, String> {

    private Pattern allowPattern;

    @Override
    public void initialize(NoForbiddenChar constraintAnnotation) {
        String forbiddenPatternStr = constraintAnnotation.forbiddenPattern();
        this.allowPattern = Pattern.compile(forbiddenPatternStr);
    }


    /**
     * @return true 不含特殊字符  false 含特殊字符
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean result = true;
        if (StringUtils.isNotEmpty(value)) {
            Matcher m = allowPattern.matcher(value);
            result = m.find();
        }
        return result;
    }
}
