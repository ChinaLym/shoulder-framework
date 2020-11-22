package org.shoulder.validate.validator;

import org.shoulder.core.util.FileUtils;
import org.shoulder.core.util.RegexpUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.validate.annotation.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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

    private static final Logger log = LoggerFactory.getLogger(FileTypeValidator.class);

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
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {

        if (multipartFile == null) {
            return allowEmpty;
        }
        String fileName = multipartFile.getOriginalFilename();
        if (!checkName(fileName)) {
            return false;
        }
        String suffix = FileUtils.getSuffix(fileName);
        log.debug("suffix is {}", suffix);
        try {
            for (String allowedSuffix : allowSuffixNameArray) {
                if (StringUtils.equals(allowedSuffix, suffix)) {
                    // 不仅仅是文件名要符合限制，还需要满足文件头限制，避免恶意文件上传
                    boolean validHeader = FileUtils.checkHeader(multipartFile.getInputStream(), allowedSuffix, true);
                    if (!validHeader || StringUtils.isEmpty(maxSizeStr)) {
                        return validHeader;
                    }
                    long allowedMaxSize = parseSize(maxSizeStr);
                    // 检查文件大小
                    long uploadSize = multipartFile.getSize();
                    return allowedMaxSize >= uploadSize;
                }
            }
        } catch (Exception e) {
            log.warn("validate mimeType fail", e);
            return false;
        }
        // 类型 / 后缀名不允许
        return false;
    }

    private long parseSize(@Nonnull CharSequence maxSizeStr) {
        return DataSize.parse(maxSizeStr).toBytes();
    }

    private boolean checkName(String fileName) {
        return (StringUtils.isEmpty(forbiddenNamePattern) || RegexpUtils.matches(fileName, allowNamePattern)) &&
            (StringUtils.isEmpty(forbiddenNamePattern) || !RegexpUtils.matches(fileName, forbiddenNamePattern));

    }

}


