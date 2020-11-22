package org.shoulder.validate.validator;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

/**
 * 支持 NotEmpty 加在 MultipartFile 上
 *
 * @author lym
 */
public class NotEmptyValidatorForMultipartFile implements ConstraintValidator<NotEmpty, MultipartFile> {

    public NotEmptyValidatorForMultipartFile() {
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        if (multipartFile == null) {
            return false;
        } else {
            return !multipartFile.isEmpty();
        }
    }
}
