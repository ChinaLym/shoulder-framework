package org.shoulder.security.code.sms.config;

import org.shoulder.code.consts.ValidateCodeConsts;
import org.shoulder.code.propertities.BaseValidateCodeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码配置项
 *
 * @author lym
 */
@ConfigurationProperties(prefix = ValidateCodeConsts.CONFIG_PREFIX + ".sms")
public class SmsCodeProperties extends BaseValidateCodeProperties {

    public SmsCodeProperties() {
        setParameterName(ValidateCodeConsts.SMS);
    }
}
