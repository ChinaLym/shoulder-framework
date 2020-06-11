package org.shoulder.security.code.sms;

import org.shoulder.code.dto.ValidateCodeDTO;
import org.shoulder.code.generator.ValidateCodeGenerator;
import org.shoulder.security.code.sms.config.SmsCodeProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 短信验证码生成器
 *
 * @author lym
 */
public class SmsCodeGenerator implements ValidateCodeGenerator, SmsValidateCodeType {

    private SmsCodeProperties smsCodeProperties;

    public SmsCodeGenerator(SmsCodeProperties smsCodeProperties) {
        this.smsCodeProperties = smsCodeProperties;
    }

    /**
     * 生成 6 位数字验证码
     */
    @Override
    public ValidateCodeDTO generate(ServletWebRequest request) {
        String code = RandomStringUtils.randomNumeric(smsCodeProperties.getLength());
        return new ValidateCodeDTO(code, smsCodeProperties.getEffectiveSeconds());
    }

    public SmsCodeProperties getSmsCodeProperties() {
        return smsCodeProperties;
    }

    public void setSmsCodeProperties(SmsCodeProperties smsCodeProperties) {
        this.smsCodeProperties = smsCodeProperties;
    }
}
