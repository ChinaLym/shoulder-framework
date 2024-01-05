package org.shoulder.code.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.code.ValidateCodeProcessorHolder;
import org.shoulder.code.consts.ValidateCodeConsts;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 验证码 Controller
 *
 * @author lym
 */
@Controller
public class ValidateCodEndpoint {

    private final ValidateCodeProcessorHolder validateCodeProcessorHolder;

    public ValidateCodEndpoint(ValidateCodeProcessorHolder validateCodeProcessorHolder) {
        this.validateCodeProcessorHolder = validateCodeProcessorHolder;
    }

    /**
     * 根据请求中的参数 type
     * 找到对应的验证码处理器
     * 交给对应的验证码处理器处理
     */
    @RequestMapping(value = ValidateCodeConsts.VALIDATE_CODE_URL, method = {RequestMethod.GET, RequestMethod.POST})
    public void createCode(HttpServletRequest request, HttpServletResponse response) {
        String type = request.getParameter(ValidateCodeConsts.VALIDATE_CODE_TYPE_PARAM_NAME);
        validateCodeProcessorHolder.getProcessor(type)
            .create(new ServletWebRequest(request, response));
    }

    public ValidateCodeProcessorHolder getValidateCodeProcessorHolder() {
        return validateCodeProcessorHolder;
    }

}
