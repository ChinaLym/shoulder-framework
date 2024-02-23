package org.shoulder.code.consts;

import org.shoulder.security.SecurityConst;

/**
 * 验证码相关的常量
 *
 * @author lym
 */
public interface ValidateCodeConsts {

    String CONFIG_PREFIX = SecurityConst.CONFIG_PREFIX + ".validate-code";
    /**
     * 获取验证码的 url 路径
     */
    String VALIDATE_CODE_URL = "/code";

    /**
     * 获取验证码请求中，用于区分验证码类型的参数名称
     */
    String VALIDATE_CODE_TYPE_PARAM_NAME = "type";


    // --------------- 默认的 -----------------

    /**
     * 图形验证码
     */
    String IMAGE = "imageCode";

    /**
     * 短信验证码
     */
    String SMS = "smsCode";

    /**
     * 短信验证码
     */
    String EMAIL = "emailCode";

    /**
     * 邀请码
     */
    String INVITATION = "invitationCode";

}
