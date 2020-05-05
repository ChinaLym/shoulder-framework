package org.shoulder.code.controller;

import org.shoulder.code.ValidateCodeProcessorHolder;
import org.shoulder.code.consts.ValidateCodeConsts;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 验证码 Controller
 * @author lym
 */
@ConditionalOnBean(ValidateCodeProcessorHolder.class)
@ConditionalOnProperty(value = ValidateCodeConsts.CONFIG_PREFIX + ".default-controller.enable", havingValue = "true", matchIfMissing = true)
@RestController
public class ValidateCodeController {

	private ValidateCodeProcessorHolder validateCodeProcessorHolder;

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

	public void setValidateCodeProcessorHolder(ValidateCodeProcessorHolder validateCodeProcessorHolder) {
		this.validateCodeProcessorHolder = validateCodeProcessorHolder;
	}
}
