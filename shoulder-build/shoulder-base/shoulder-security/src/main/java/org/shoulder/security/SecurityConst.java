package org.shoulder.security;

/**
 * @author lym
 */
public interface SecurityConst {

    String CONFIG_PREFIX = "shoulder.security";

    /**
     * 表单方式认证（用户名密码登录）
     */
    String URL_AUTHENTICATION_FORM = "/authentication/form";

    /**
     * 短信验证码认证（短信验证码登录）
     */
    String URL_AUTHENTICATION_SMS = "/authentication/sms";

    /**
     * 注册新用户 url
     */
    String URL_REGISTER = "/user/register";

    /**
     * 取消认证（退出登录）
     */
    String URL_AUTHENTICATION_CANCEL = "/authentication/cancel";


    /**
     * 获取验证码请求 url
     */
    String URL_VALIDATE_CODE = "/code";

    /**
     * 当请求需要身份认证时，默认跳转的url
     */
    String URL_REQUIRE_AUTHENTICATION = "/authentication/require";

    String AUTHENTICATION_SMS_PARAMETER_NAME = "phoneNumber";


    /**
     * 浏览器相关常量
     *
     * @author lym
     */
    interface DefaultPage {

        /**
         * 登录门户
         */
        String SIGN_IN = "/signIn.html";
        /**
         * 注册门户
         */
        String SIGN_UP = "/signUp.html";
        /**
         *
         */
        String SESSION_INVALID = SIGN_IN;
    }


}
