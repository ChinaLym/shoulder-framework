package org.shoulder.validate.annotation;

import org.shoulder.validate.validator.NoForbiddenCharValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 禁止特殊字符注解（禁用名单）
 * 使用：加在 CharSequence 类型的字段上
 *
 * @author lym
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {NoForbiddenCharValidator.class})
public @interface NoForbiddenChar {

    String forbiddenPattern() default "[\\\\/:*?\"<|'>]";

    /**
     * 默认提示为 input.special.character 翻译为：参数中包含了特殊字符
     */
    String message() default "input.special.character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
