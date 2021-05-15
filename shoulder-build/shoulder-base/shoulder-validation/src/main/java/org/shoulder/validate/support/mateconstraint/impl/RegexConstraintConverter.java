package org.shoulder.validate.support.mateconstraint.impl;

import org.hibernate.validator.constraints.URL;
import org.shoulder.validate.support.mateconstraint.ConstraintConverter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * 正则校验规则
 *
 * @author lym
 */
public class RegexConstraintConverter extends BaseConstraintConverter implements ConstraintConverter {

    public RegexConstraintConverter() {
        supportAnnotations = Arrays.asList(Pattern.class, Email.class, URL.class);
        ;
        methods = Arrays.asList(
                // 正则表达式
                "regexp",
                // 校验失败提示
                "message"
        );
    }

    @Override
    protected String getType(Class<? extends Annotation> type) {
        return "Regex";
    }

}
