package org.shoulder.validate.annotation;

import org.shoulder.validate.validator.FileTypeValidator;
import org.shoulder.validate.validator.FileTypeValidatorForArray;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 校验上传文件的 mimeType
 * <p>
 * 使用：加在 {@link MultipartFile} 类型的字段上
 *
 * @author lym
 */
@Inherited
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {FileTypeValidator.class, FileTypeValidatorForArray.class})
public @interface FileType {

    /**
     * 允许的文件后缀名
     */
    String[] allowSuffix();

    /**
     * 允许的单文件最大大小
     * 为空时不限制，注意该校验为逻辑校验，在此之前还有 nginx 接收请求大小限制、servlet 接收请求大小限制
     * 注意必须大写字母，且以B结尾，如 B、KB、MB、GB，不能使用小写，不能简写去掉B
     *
     * @see org.springframework.util.unit.DataSize#parse(java.lang.CharSequence, org.springframework.util.unit.DataUnit)
     */
    String maxSize() default "";

    /**
     * 文件名限制，必须符合该格式，支持正则，为空则不校验
     */
    String nameAllowPattern() default "";

    /**
     * 非法文件名格式，不得出现该格式，支持正则，为空则不校验
     */
    String nameForbiddenPattern() default "";

    /**
     * 是否允许为空
     * 默认允许
     */
    boolean allowEmpty() default true;

    /**
     * 提示信息对应的翻译key
     */
    String message() default "shoulder.validate.input.mimeType.illegal";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
