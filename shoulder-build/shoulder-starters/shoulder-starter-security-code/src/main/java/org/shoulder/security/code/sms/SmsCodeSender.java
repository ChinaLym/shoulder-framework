package org.shoulder.security.code.sms;

/**
 * 短信验证码发送者
 *
 * @author lym
 */
public interface SmsCodeSender {

    /**
     * 发送短信验证码
     * @param mobile 手机号
     * @param code 验证码
     * @throws Exception 发送失败
     */
    void send(String mobile, String code) throws Exception;

}
