package org.shoulder.validate.annotation;

import org.shoulder.validate.validator.NoForbiddenCharValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 仅出现在枚举中
 * 使用：加在 CharSequence 类型的字段上
 *
 * @author lym
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {NoForbiddenCharValidator.class})
public @interface Enum {

    /**
     * 默认提示翻译为：参数输入不在预期范围之内
     */
    String message() default "input.notExcepted";

    /**
     * 允许的值
     */
    String[] enums() default {};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
