package org.shoulder.security.code.sms.config;

import org.shoulder.code.consts.ValidateCodeConsts;
import org.shoulder.code.propertities.BaseValidateCodeProperties;
import org.shoulder.security.SecurityConst;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedList;
import java.util.List;

/**
 * 验证码配置项
 *
 * @author lym
 */
@ConfigurationProperties(prefix = ValidateCodeConsts.CONFIG_PREFIX + ".sms")
public class SmsCodeProperties extends BaseValidateCodeProperties {

    public SmsCodeProperties() {
        setParameterName(ValidateCodeConsts.SMS);
        List<String> defaultValidateUrls = new LinkedList<>();
        // 默认短信登录 url 需要校验短信验证码，可通过修改 "shoulder.security.validate-code.sms.urls" 来修改/新增需要校验的路径
        defaultValidateUrls.add(SecurityConst.URL_AUTHENTICATION_SMS);
        setUrls(defaultValidateUrls);
    }
}
