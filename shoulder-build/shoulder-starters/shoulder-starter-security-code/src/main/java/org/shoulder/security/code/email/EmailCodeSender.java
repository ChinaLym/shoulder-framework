package org.shoulder.security.code.email;

/**
 * 短信验证码发送者
 *
 * @author lym
 */
public interface EmailCodeSender {

    /**
     * 发送短信验证码
     *
     * @param email 手机号
     * @param code  验证码
     * @throws Exception 发送失败
     */
    void send(String email, String code) throws Exception;

}
