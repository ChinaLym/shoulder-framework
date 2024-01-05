package org.shoulder.validate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

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
