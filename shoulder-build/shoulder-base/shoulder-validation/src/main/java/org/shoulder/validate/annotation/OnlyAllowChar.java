package org.shoulder.validate.annotation;

import org.shoulder.validate.validator.OnlyAllowCharValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 只允许特定的字符出现（白名单）
 * 使用：加在 String 类型的字段上
 * 更推荐使用 JSR 303 规范中的 {@link javax.validation.constraints.Pattern}
 *
 * @author lym
 */
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
