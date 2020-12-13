package org.shoulder.validate.annotation;

import org.shoulder.validate.validator.NotAllFieldsNullValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

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
