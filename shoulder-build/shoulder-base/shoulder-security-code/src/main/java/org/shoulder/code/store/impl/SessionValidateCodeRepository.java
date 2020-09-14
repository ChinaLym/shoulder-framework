package org.shoulder.code.store.impl;

import org.shoulder.code.dto.ValidateCodeDTO;
import org.shoulder.code.store.ValidateCodeStore;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 基于session的验证码存取器
 *
 * @author lym
 */
public class SessionValidateCodeRepository implements ValidateCodeStore {

    /**
     * 验证码放入session时的前缀
     */
    private static final String DEFAULT_KEY_PREFIX = "CAPTCHA_CODE:";

    @Override
    public void save(ServletWebRequest request, ValidateCodeDTO code, String validateCodeType) {
        request.getRequest().getSession().setAttribute(buildSessionKey(validateCodeType), code);
    }


    @Override
    public ValidateCodeDTO get(ServletWebRequest request, String validateCodeType) {
        return (ValidateCodeDTO) request.getRequest().getSession().getAttribute(buildSessionKey(validateCodeType));
    }

    @Override
    public void remove(ServletWebRequest request, String codeType) {
        request.getRequest().getSession().removeAttribute(buildSessionKey(codeType));
    }

    /**
     * 验证码放入 session 的 key
     */
    protected String buildSessionKey(String validateCodeType) {
        return DEFAULT_KEY_PREFIX + validateCodeType.toUpperCase();
    }

}
