package org.shoulder.validate.validator;

import org.apache.tika.Tika;
import org.shoulder.validate.consant.MIMEEnum;
import org.shoulder.validate.annotation.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@link MimeType} 注解校验处理
 * 上传文件使用 @RequestParam 时，hibernate validate校验参考：
 *  <a href="https://www.cnblogs.com/pangguoming/p/8967910.html"/>
 *  <a href="https://my.oschina.net/u/2608182/blog/1647384"/>
 * @author lym
 */
public class MimeTypeValidator implements ConstraintValidator<MimeType, MultipartFile> {

    private static final Logger log = LoggerFactory.getLogger(MimeTypeValidator.class);

    private MIMEEnum[] mimeTypeArray = {};

    @Override
    public void initialize(MimeType constraintAnnotation) {
        mimeTypeArray = constraintAnnotation.allowList();
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


