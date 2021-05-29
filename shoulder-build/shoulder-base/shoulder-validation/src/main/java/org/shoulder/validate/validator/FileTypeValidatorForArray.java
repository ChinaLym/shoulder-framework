package org.shoulder.validate.validator;

import org.shoulder.validate.annotation.FileType;
import org.shoulder.validate.util.FileValidator;
import org.shoulder.validate.util.FileValidatorProperties;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@link FileType} 注解校验处理，支持数组
 *
 * @author lym
 * @see FileTypeValidator
 */
public class FileTypeValidatorForArray implements ConstraintValidator<FileType, MultipartFile[]> {

    private FileValidatorProperties fileValidatorProperties;

    @Override
    public void initialize(FileType constraintAnnotation) {
        fileValidatorProperties = new FileValidatorProperties(constraintAnnotation);
    }

    @Override
    public boolean isValid(MultipartFile[] multipartFiles, ConstraintValidatorContext context) {
        for (int i = 0; i < multipartFiles.length; i++) {
            MultipartFile file = multipartFiles[i];
            if (!FileValidator.isValid(fileValidatorProperties, file)) {
                return false;
            }
        }
        return true;
    }


}


