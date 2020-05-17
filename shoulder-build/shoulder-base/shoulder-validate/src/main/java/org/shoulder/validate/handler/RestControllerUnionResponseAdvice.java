package org.shoulder.validate.handler;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.validate.annotation.SkipResponseWrap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * 统一接口返回值
 * 默认自动将 RestController 的返回值用 {@link org.shoulder.core.dto.response.BaseResponse} 包装。
 * 如果某个方法不希望被包装，则添加 todo 注解
 * 如果希望使用自己项目中的返回值类，返回值继承 {@link org.shoulder.core.dto.response.BaseResponse} 类即可。
 *
 * todo 统一加密，签名？
 *
 * @author lym
 */
@Configuration
@RestControllerAdvice
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "shoulder.web.unionResponse", havingValue = "true", matchIfMissing = true)
public class RestControllerUnionResponseAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		SkipResponseWrap skip = Objects.requireNonNull(returnType.getMethod()).getAnnotation(SkipResponseWrap.class);
		// 对且仅对没有添加 @SkipResponseWrap 且返回值为 json 的接口生效
		return skip == null && MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		if (body == null) {
			return BaseResponse.success();
		}
		if (BaseResponse.class.isAssignableFrom(body.getClass())) {
			return body;
		}
		return BaseResponse.success().setData(body);
	}
	
}