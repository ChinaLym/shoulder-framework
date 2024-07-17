package org.shoulder.validate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.shoulder.validate.validator.EnumValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仅出现在枚举中
 * 使用：加在 CharSequence 类型的字段上
 *
 * @author lym
 */
@Inherited
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {EnumValidator.class})
public @interface Enum {

    /**
     * 允许的值
     */
    String[] enums() default {};

    /**
     * 提示信息对应的翻译key
     */
    String message() default "shoulder.validate.input.notExcepted";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
