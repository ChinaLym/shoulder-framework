package org.shoulder.validate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.shoulder.validate.validator.NoForbiddenCharValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 禁止特殊字符注解（禁用名单）
 * 使用：加在 CharSequence 类型的字段上
 *
 * @author lym
 */
@Inherited
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {NoForbiddenCharValidator.class})
public @interface NoForbiddenChar {

    /**
     * 禁止包含的字符
     */
    String forbiddenPattern() default "[\\\\/:*?\"<|'>]";

    /**
     * 提示信息
     */
    String message() default "shoulder.validate.input.specialCharacter";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
