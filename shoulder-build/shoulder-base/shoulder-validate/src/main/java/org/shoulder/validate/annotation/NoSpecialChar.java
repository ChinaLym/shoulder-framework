package org.shoulder.validate.annotation;

import org.shoulder.validate.validator.NoSpecialCharValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 禁止特殊字符校验
 *
 * @author lym
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {NoSpecialCharValidator.class})
public @interface NoSpecialChar {

    /**
     * 默认提示为 input.special.charactor 翻译为：参数中包含了特殊字符
     */
    String message() default "input.special.charactor";

    String forbiddenPattern() default "[\\\\/:*?\"<|'>]";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
