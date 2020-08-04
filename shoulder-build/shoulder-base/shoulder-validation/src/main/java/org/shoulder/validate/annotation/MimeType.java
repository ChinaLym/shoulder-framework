package org.shoulder.validate.annotation;


import org.shoulder.validate.consant.MIMEEnum;
import org.shoulder.validate.validator.MimeTypeValidator;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 校验上传文件的 mimeType
 *
 * 使用：加在 {@link MultipartFile} 类型的字段上
 *
 * @author lym
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MimeTypeValidator.class)
public @interface MimeType {

    /** 允许的 mimeType 类型 */
    MIMEEnum[] allowList();

    /**
     * 提示信息对应的翻译key
     */
    String message() default "shoulder.validate.input.mimeType.illegal";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
