package org.shoulder.validate.annotation;

import org.shoulder.validate.validator.FileTypeValidator;
import org.shoulder.validate.validator.JsonTypeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 校验某 String 属性 不为空时，是否为 json 类型
 *
 * @author lym
 */
@Inherited
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {FileTypeValidator.class, JsonTypeValidator.class})
public @interface JsonType {

    /**
     * 提示信息对应的翻译key
     */
    String message() default "shoulder.validate.input.mimeType.illegal";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
