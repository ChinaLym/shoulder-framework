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
     * 短信验证码认证（邮件验证码登录）
     */
    String URL_AUTHENTICATION_EMAIL = "/authentication/email";

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
     * 当请求需要身份认证时，默认跳转的url，该路由必须允许访问，否则将导致重定向次数过多
     */
    String URL_REQUIRE_AUTHENTICATION = "/authentication/require";

    String AUTHENTICATION_SMS_PARAMETER_NAME = "phoneNumber";

    String AUTHENTICATION_EMAIL_PARAMETER_NAME = "email";


    String AUTH_FAIL_PARAM_NAME = "_auth_fail_reason";


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
