package org.shoulder.validate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.shoulder.validate.validator.FileTypeValidator;
import org.shoulder.validate.validator.JsonTypeValidator;

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

    FormType formType() default FormType.DEFAULT;

    static enum FormType {

        /**
         * 只要能被JSON PARSE就是合法的
         */
        DEFAULT,

        /**
         * 校验Json串能否转换成一个对象
         */
        OBJECT,

        /**
         * 校验Json串能否转换成一个MAP
         */
        MAP,

        /**
         * 校验Json串能否转换成一个LIST
         */
        LIST;

        FormType() {
        }
    }
}
