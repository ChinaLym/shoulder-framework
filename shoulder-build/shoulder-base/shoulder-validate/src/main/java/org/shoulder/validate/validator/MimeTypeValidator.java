package org.shoulder.validate.validator;

import org.apache.tika.Tika;
import org.shoulder.validate.consant.MIMEEnum;
import org.shoulder.validate.annotation.MimeTypeValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@link @MimeTypeValidate} 注解处理
 * 上传文件使用 @RequestParam 时，hibernate validate校验参考：
 *  <@link https://www.cnblogs.com/pangguoming/p/8967910.html/>
 *  <@link https://my.oschina.net/u/2608182/blog/1647384/>
 * @author lym
 */
public class MimeTypeValidator implements ConstraintValidator<MimeTypeValidate, MultipartFile> {

    private static final Logger log = LoggerFactory.getLogger(MimeTypeValidator.class);

    private MIMEEnum[] mimeTypeArray = {};

    @Override
    public void initialize(MimeTypeValidate constraintAnnotation) {
        mimeTypeArray = constraintAnnotation.mimeType();
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        Tika tika = new Tika();
        String fileName = multipartFile.getOriginalFilename();
        String mimeType = tika.detect(fileName);
        log.debug("mimeType is {}", mimeType);

        for (MIMEEnum mimeEnum : mimeTypeArray) {
            if (mimeEnum.getMimeType().equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

}


