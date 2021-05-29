package org.shoulder.validate.util;

import lombok.Data;
import org.shoulder.validate.annotation.FileType;

/**
 * properties
 *
 * @author lym
 */
@Data
public class FileValidatorProperties {

    private String[] allowSuffixNameArray = {};

    private String maxSizeStr = "";

    private boolean allowEmpty;

    private String allowNamePattern;

    private String forbiddenNamePattern;

    public FileValidatorProperties(FileType constraintAnnotation) {
        allowSuffixNameArray = constraintAnnotation.allowSuffix();
        maxSizeStr = constraintAnnotation.maxSize();
        allowEmpty = constraintAnnotation.allowEmpty();
        allowNamePattern = constraintAnnotation.nameAllowPattern();
        forbiddenNamePattern = constraintAnnotation.nameForbiddenPattern();
    }

}
