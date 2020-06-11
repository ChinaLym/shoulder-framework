package org.shoulder.security.code.img;

import org.shoulder.code.exception.ValidateCodeException;
import org.shoulder.code.generator.ValidateCodeGenerator;
import org.shoulder.code.processor.AbstractValidateCodeProcessor;
import org.shoulder.code.propertities.BaseValidateCodeProperties;
import org.shoulder.code.store.ValidateCodeStore;
import org.springframework.web.context.request.ServletWebRequest;

import javax.imageio.ImageIO;
import java.util.Objects;

/**
 * 图片验证码处理器
 * @author lym
 */
public class ImageCodeProcessor extends AbstractValidateCodeProcessor<ImageCode> implements ImageValidateCodeType {

	public ImageCodeProcessor(BaseValidateCodeProperties baseValidateCodeProperties, ValidateCodeGenerator validateCodeGenerator, ValidateCodeStore validateCodeStore) {
		super(baseValidateCodeProperties, validateCodeGenerator, validateCodeStore);
	}

	/**
	 * 发送图形验证码，将其写到响应中
	 */
	@Override
	public void send(ServletWebRequest request, ImageCode imageCode) throws ValidateCodeException {
		try{
			ImageIO.write(imageCode.getImage(), "JPEG", Objects.requireNonNull(request.getResponse()).getOutputStream());
		}catch(Exception e){
			throw new ValidateCodeException("send validate code fail.", e);
		}
	}

}
