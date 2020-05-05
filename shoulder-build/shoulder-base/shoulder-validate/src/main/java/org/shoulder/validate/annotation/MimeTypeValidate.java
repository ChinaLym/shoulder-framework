package org.shoulder.validate.annotation;


import org.shoulder.validate.consant.MIMEEnum;
import org.shoulder.validate.validator.MimeTypeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验上传文件的 mimeType
 * @author lym
 */
@Constraint(validatedBy = MimeTypeValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MimeTypeValidate {

    String message() default "mimeType is not support";

    /** mimeType 白名单 */
    MIMEEnum[] mimeType();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
