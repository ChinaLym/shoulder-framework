package org.shoulder.validate.validator;

import org.shoulder.validate.annotation.FileType;
import org.shoulder.validate.util.FileValidator;
import org.shoulder.validate.util.FileValidatorProperties;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link FileType} 注解校验处理
 * 上传文件使用 @RequestParam 时，hibernate validate校验参考：
 * <a href="https://www.cnblogs.com/pangguoming/p/8967910.html"/>
 * <a href="https://my.oschina.net/u/2608182/blog/1647384"/>
 * <p>
 * MagicMatch
 *
 * @author lym
 */
public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {


    private FileValidatorProperties fileValidatorProperties;

    @Override
    public void initialize(FileType constraintAnnotation) {
        fileValidatorProperties = new FileValidatorProperties(constraintAnnotation);
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        return FileValidator.isValid(fileValidatorProperties, multipartFile);
    }

}


