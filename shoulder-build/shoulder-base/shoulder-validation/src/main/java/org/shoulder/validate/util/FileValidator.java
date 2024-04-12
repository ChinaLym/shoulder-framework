package org.shoulder.validate.util;

import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.FileUtils;
import org.shoulder.core.util.RegexpUtils;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lym
 */
public class FileValidator {

    private static final Logger log = ShoulderLoggers.SHOULDER_DEFAULT;

    public static boolean isValid(FileValidatorProperties properties, MultipartFile multipartFile) {
        if (multipartFile == null) {
            return properties.isAllowEmpty();
        }
        String fileName = multipartFile.getOriginalFilename();
        log.debug("fileName is {}", fileName);
        if (!isValidName(properties, fileName)) {
            log.warn("illegal fileName:" + fileName);
            return false;
        }
        String suffix = FileUtils.getSuffix(fileName);
        log.debug("suffix is {}", suffix);
        try {
            for (String allowedSuffix : properties.getAllowSuffixNameArray()) {
                if (StringUtils.equals(allowedSuffix, suffix)) {
                    // 不仅仅是文件名要符合限制，还需要满足文件头限制，避免恶意文件上传
                    boolean validHeader = FileUtils.checkHeader(multipartFile.getInputStream(), allowedSuffix, true);
                    if (!validHeader) {
                        log.warn("illegal fileHeader:" + fileName);
                        return false;
                    }
                    // 检查文件大小
                    long uploadSize = multipartFile.getSize();
                    if (StringUtils.isEmpty(properties.getMaxSizeStr())) {
                        log.debug("PASS ignore fileSize, received bytes: {}", fileName);
                        return true;
                    }
                    long allowedMaxSize = DataSize.parse(properties.getMaxSizeStr()).toBytes();
                    if (allowedMaxSize >= uploadSize) {
                        log.debug("PASS validate: {}", fileName);
                        return true;
                    } else {
                        log.warn("the file({}) size({}) exceed max({})", fileName, uploadSize, properties.getMaxSizeStr());
                        return false;
                    }
                }
            }
            // 类型 / 后缀名不允许(不在白名单)
            log.warn("illegal fileSuffix:{} sourceFileName:{}", suffix, fileName);
            return false;
        } catch (Exception e) {
            log.warn("validate mimeType fail", e);
            return false;
        }
    }

    static boolean isValidName(FileValidatorProperties properties, String fileName) {
        // no allowNamePattern or match
        // no forbiddenNamePattern or disMatch
        return (StringUtils.isEmpty(properties.getAllowNamePattern()) || RegexpUtils.matches(fileName, properties.getAllowNamePattern())) &&
               (StringUtils.isEmpty(properties.getForbiddenNamePattern()) || !RegexpUtils.matches(fileName,
                   properties.getForbiddenNamePattern()));

    }

}
