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

    /**
     * 默认提示为 input.mimeType.illegal 翻译为：上传文件格式非法
     */
    String message() default "input.mimeType.illegal";

    /** mimeType 白名单 */
    MIMEEnum[] whiteList();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
