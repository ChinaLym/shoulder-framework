package org.shoulder.security.code.sms;

/**
 * 短信验证码发送者
 * @author lym
 */
public interface SmsCodeSender {
	
	void send(String mobile, String code) throws Exception;

}
