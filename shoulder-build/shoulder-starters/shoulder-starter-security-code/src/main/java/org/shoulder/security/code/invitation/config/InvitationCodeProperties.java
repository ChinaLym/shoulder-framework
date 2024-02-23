package org.shoulder.security.code.invitation.config;

import org.shoulder.code.consts.ValidateCodeConsts;
import org.shoulder.code.propertities.BaseValidateCodeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedList;
import java.util.List;

/**
 * 验证码配置项
 *
 * @author lym
 */
@ConfigurationProperties(prefix = ValidateCodeConsts.CONFIG_PREFIX + ".invitation")
public class InvitationCodeProperties extends BaseValidateCodeProperties {

    public InvitationCodeProperties() {
        setParameterName(ValidateCodeConsts.INVITATION);
        List<String> defaultValidateUrls = new LinkedList<>();
        // 默认invitation登录 url 需要校验invitation验证码，可通过修改 "shoulder.security.validate-code.invitation.urls" 来修改/新增需要校验的路径
        defaultValidateUrls.add("/authentication/invitation");
        setUrls(defaultValidateUrls);
    }
}
