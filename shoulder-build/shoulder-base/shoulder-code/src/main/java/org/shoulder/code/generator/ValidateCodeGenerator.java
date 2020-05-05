package org.shoulder.code.generator;

import org.shoulder.code.ValidateCodeType;
import org.shoulder.code.dto.ValidateCodeDTO;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * 验证码生成器
 * @author lym
 */
public interface ValidateCodeGenerator extends ValidateCodeType {

	ValidateCodeDTO generate(ServletWebRequest request);
	
}
