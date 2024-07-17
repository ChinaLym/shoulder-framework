package org.shoulder.validate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.shoulder.validate.validator.OnlyAllowCharValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 只允许特定的字符出现（白名单）
 * 使用：加在 String 类型的字段上
 * 更推荐使用 JSR 303 规范中的 {@link jakarta.validation.constraints.Pattern}
 *
 * @author lym
 */
@Inherited
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {OnlyAllowCharValidator.class})
public @interface OnlyAllowChar {

    /**
     * 白名单正则，默认只允许字母数字下划线
     */
    String allowPattern() default "[a-zA-Z0-9_-]";

    /**
     * 提示信息
     */
    String message() default "shoulder.validate.input.specialCharacter";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
