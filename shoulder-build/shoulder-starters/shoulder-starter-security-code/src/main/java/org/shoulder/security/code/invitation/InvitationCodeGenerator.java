package org.shoulder.security.code.invitation;

import org.apache.commons.lang3.RandomStringUtils;
import org.shoulder.code.dto.ValidateCodeDTO;
import org.shoulder.code.generator.ValidateCodeGenerator;
import org.shoulder.security.code.invitation.config.InvitationCodeProperties;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 邀请码生成器
 *
 * @author lym
 */
public class InvitationCodeGenerator implements ValidateCodeGenerator, InvitationCodeType {

    private InvitationCodeProperties invitationCodeProperties;

    public InvitationCodeGenerator(InvitationCodeProperties invitationCodeProperties) {
        this.invitationCodeProperties = invitationCodeProperties;
    }

    /**
     * 生成 6 位数字验证码
     */
    @Override
    public ValidateCodeDTO generate(ServletWebRequest request) {
        String code = RandomStringUtils.randomNumeric(invitationCodeProperties.getLength());
        // 参考：https://blog.csdn.net/K346K346/article/details/120131390
        // https://blog.frostmiku.com/archives/33/
        return new ValidateCodeDTO(code, invitationCodeProperties.getEffectiveSeconds());
    }

    public InvitationCodeProperties getInvitationCodeProperties() {
        return invitationCodeProperties;
    }

    public void setInvitationCodeProperties(InvitationCodeProperties invitationCodeProperties) {
        this.invitationCodeProperties = invitationCodeProperties;
    }
}
