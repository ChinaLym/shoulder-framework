package org.shoulder.code.generator;

import org.shoulder.code.ValidateCodeType;
import org.shoulder.code.dto.ValidateCodeDTO;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 验证码生成器
 * @author lym
 */
public interface ValidateCodeGenerator extends ValidateCodeType {

	/**
	 * 生成验证码
	 * @param request 请求，可以从这里获取参数
	 * @return 验证码 DTO
	 */
	ValidateCodeDTO generate(ServletWebRequest request);
	
}
