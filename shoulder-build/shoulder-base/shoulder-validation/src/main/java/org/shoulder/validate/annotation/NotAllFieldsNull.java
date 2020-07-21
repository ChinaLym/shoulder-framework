package org.shoulder.validate.annotation;

import org.shoulder.validate.validator.NotAllFieldsNullValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 多个字段不能同时为空，至少一个字段不能为空
 * 使用：加在需要校验的类上，填写 {@link #fields}
 * @author lym
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NotAllFieldsNullValidator.class)
@Documented
public @interface NotAllFieldsNull {

    /**
     * 需要校验的字段，这几个字段不能同时为空，至少有一个非空才行
     */
    String[] fields();

    /** 默认错误消息 */
    String message() default "";

    /** 分组 */
    Class<?>[] groups() default {};

    /** 负载 */
    Class<? extends Payload>[] payload() default {};
}
