package org.shoulder.security.code.invitation;

import jakarta.annotation.Nullable;
import org.shoulder.code.dto.ValidateCodeDTO;
import org.shoulder.code.exception.ValidateCodeException;
import org.shoulder.code.generator.ValidateCodeGenerator;
import org.shoulder.code.processor.AbstractValidateCodeProcessor;
import org.shoulder.code.propertities.BaseValidateCodeProperties;
import org.shoulder.code.store.ValidateCodeStore;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * invitation验证码处理器
 *
 * @author lym
 */
public class InvitationCodeProcessor extends AbstractValidateCodeProcessor<ValidateCodeDTO> implements InvitationCodeType {

    public InvitationCodeProcessor(@Nullable BaseValidateCodeProperties validateCodeProperties,
                                   ValidateCodeGenerator validateCodeGenerator, ValidateCodeStore validateCodeStore) {
        super(validateCodeProperties, validateCodeGenerator, validateCodeStore);
    }

    /**
     * invitation验证码只有 POST 请求才能获取（避免直接在浏览器地址栏上直接访问调用）
     */
    @Override
    protected boolean isPostOnly() {
        return true;
    }

    @Override
    public void send(ServletWebRequest request, ValidateCodeDTO validateCode) throws ValidateCodeException {
        try {
            String activityId = ServletRequestUtils.getRequiredStringParameter(request.getRequest(),
                    "activityId");
            // 写 json 到 response
            //invitationCodeSender.send(mobile, validateCode.getCode());
        } catch (Exception e) {
            throw new ValidateCodeException("send invitationCode fail", e);
        }
    }

}
