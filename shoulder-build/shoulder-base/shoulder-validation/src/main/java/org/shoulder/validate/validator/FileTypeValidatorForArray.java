package org.shoulder.validate.validator;

import org.shoulder.core.util.FileUtils;
import org.shoulder.core.util.RegexpUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.validate.annotation.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.unit.DataSize;
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

    private static final Logger log = LoggerFactory.getLogger(FileTypeValidatorForArray.class);

    private String[] allowSuffixNameArray = {};

    private String maxSizeStr = "";

    private boolean allowEmpty;

    private String allowNamePattern;

    private String forbiddenNamePattern;

    @Override
    public void initialize(FileType constraintAnnotation) {
        allowSuffixNameArray = constraintAnnotation.allowSuffix();
        maxSizeStr = constraintAnnotation.maxSize();
        allowEmpty = constraintAnnotation.allowEmpty();
        allowNamePattern = constraintAnnotation.nameAllowPattern();
        forbiddenNamePattern = constraintAnnotation.nameForbiddenPattern();
    }

    @Override
    public boolean isValid(MultipartFile[] multipartFiles, ConstraintValidatorContext context) {
        if (multipartFiles == null || multipartFiles.length == 0) {
            return allowEmpty;
        }
        for (int i = 0; i < multipartFiles.length; i++) {
            MultipartFile file = multipartFiles[i];
            String fileName = file.getOriginalFilename();
            if (!checkName(fileName)) {
                return false;
            }
            String suffix = FileUtils.getSuffix(fileName);
            log.debug("multipartFiles[{}].suffix is {}", i, suffix);

            try {
                for (String allowedSuffix : allowSuffixNameArray) {
                    if (StringUtils.equals(allowedSuffix, suffix)) {
                        // 不仅仅是文件名要符合限制，还需要满足文件头限制，避免恶意文件上传
                        boolean validHeader = FileUtils.checkHeader(file.getInputStream(), allowedSuffix, true);
                        if (!validHeader) {
                            return false;
                        }
                        if (StringUtils.isNotEmpty(maxSizeStr)) {
                            // 检查文件大小
                            long allowedMaxSize = DataSize.parse(maxSizeStr).toBytes();
                            long uploadSize = file.getSize();
                            return allowedMaxSize >= uploadSize;
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                log.warn("validate suffix fail", e);
                return false;
            }
        }
        // 类型 / 后缀名不允许
        return false;
    }

    private boolean checkName(String fileName) {
        return (StringUtils.isEmpty(forbiddenNamePattern) || RegexpUtils.matches(fileName, allowNamePattern)) &&
            (StringUtils.isEmpty(forbiddenNamePattern) || !RegexpUtils.matches(fileName, forbiddenNamePattern));

    }

}


