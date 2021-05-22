package org.shoulder.security.code.email;

import org.apache.commons.lang3.RandomStringUtils;
import org.shoulder.code.dto.ValidateCodeDTO;
import org.shoulder.code.generator.ValidateCodeGenerator;
import org.shoulder.security.code.email.config.EmailCodeProperties;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 短信验证码生成器
 *
 * @author lym
 */
public class EmailCodeGenerator implements ValidateCodeGenerator, EmailValidateCodeType {

    private EmailCodeProperties emailCodeProperties;

    public EmailCodeGenerator(EmailCodeProperties emailCodeProperties) {
        this.emailCodeProperties = emailCodeProperties;
    }

    /**
     * 生成 6 位数字验证码
     */
    @Override
    public ValidateCodeDTO generate(ServletWebRequest request) {
        String code = RandomStringUtils.randomNumeric(emailCodeProperties.getLength());
        return new ValidateCodeDTO(code, emailCodeProperties.getEffectiveSeconds());
    }

    public EmailCodeProperties getEmailCodeProperties() {
        return emailCodeProperties;
    }

    public void setEmailCodeProperties(EmailCodeProperties emailCodeProperties) {
        this.emailCodeProperties = emailCodeProperties;
    }
}
