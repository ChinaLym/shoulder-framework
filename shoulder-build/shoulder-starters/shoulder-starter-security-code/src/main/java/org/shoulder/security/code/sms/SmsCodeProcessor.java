package org.shoulder.security.code.sms;

import org.shoulder.code.dto.ValidateCodeDTO;
import org.shoulder.code.exception.ValidateCodeException;
import org.shoulder.code.generator.ValidateCodeGenerator;
import org.shoulder.code.processor.AbstractValidateCodeProcessor;
import org.shoulder.code.propertities.BaseValidateCodeProperties;
import org.shoulder.code.store.ValidateCodeStore;
import org.shoulder.security.SecurityConst;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 短信验证码处理器
 *
 * @author lym
 */
public class SmsCodeProcessor extends AbstractValidateCodeProcessor<ValidateCodeDTO> implements SmsValidateCodeType {

    /**
     * 短信验证码发送器
     */
    private SmsCodeSender smsCodeSender;

    public SmsCodeProcessor(BaseValidateCodeProperties baseValidateCodeProperties, ValidateCodeGenerator validateCodeGenerator,
                            ValidateCodeStore validateCodeStore, SmsCodeSender smsCodeSender) {
        super(baseValidateCodeProperties, validateCodeGenerator, validateCodeStore);
        this.smsCodeSender = smsCodeSender;
    }

    /**
     * 短信验证码只有 POST 请求才能获取（避免直接在浏览器地址栏上直接访问调用）
     */
    @Override
    protected boolean isPostOnly() {
        return true;
    }

    @Override
    public void send(ServletWebRequest request, ValidateCodeDTO validateCode) throws ValidateCodeException {
        try {
            String mobile = ServletRequestUtils.getRequiredStringParameter(request.getRequest(), SecurityConst.AUTHENTICATION_SMS_PARAMETER_NAME);
            smsCodeSender.send(mobile, validateCode.getCode());
        } catch (Exception e) {
            throw new ValidateCodeException("send smsCode fail", e);
        }
    }

}
