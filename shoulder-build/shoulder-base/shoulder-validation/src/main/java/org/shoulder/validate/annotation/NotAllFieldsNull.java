package org.shoulder.validate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.shoulder.validate.validator.NotAllFieldsNullValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 多个字段不能同时为空，至少一个字段不能为空
 * 使用：加在需要校验的类上，填写 {@link #fields}
 *
 * @author lym
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NotAllFieldsNullValidator.class)
@Documented
public @interface NotAllFieldsNull {

    /**
     * 不能同时为空的字段名
     */
    String[] fields();

    /**
     * 提示信息
     */
    String message() default "shoulder.validate.input.allEmpty";

    /**
     * 分组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};
}
