package org.shoulder.web.advice;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * 统一接口返回值
 * 默认自动将 RestController 的返回值用 {@link BaseResponse} 包装。
 * 如果某个方法不希望被包装，则添加 {@link SkipResponseWrap}，也可以直接在类上添加
 * 如果希望使用自己项目中的返回值类，返回值继承 {@link BaseResponse} 类即可。
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
	public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return needWrapResponse(returnType, converterType);
	}

	@Override
	public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
		if (body == null) {
			return BaseResponse.success();
		}
		if (BaseResponse.class.isAssignableFrom(body.getClass())) {
			return body;
		}
		return BaseResponse.success().setData(body);
	}

    /**
     * 是否需要包装返回值，自动包装必须符合以下条件
     *      接口返回值格式化类型为 Json
     *      不是 Spring 的标准返回值类型 {@link ResponseEntity}
     *      方法或类上未添加 {@link SkipResponseWrap}
     */
    private boolean needWrapResponse(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        boolean jsonType = MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
        boolean springStdResponseType = ResponseEntity.class.isAssignableFrom(returnType.getParameterType());
        if (!jsonType || springStdResponseType){
            return false;
        }

        boolean withoutMethodAnnotation = returnType.getMethodAnnotation(SkipResponseWrap.class) == null;
        boolean withoutClassAnnotation = AnnotatedElementUtils.findMergedAnnotation(returnType.getContainingClass(),
            SkipResponseWrap.class) == null;
        return withoutMethodAnnotation && withoutClassAnnotation;

    }
	
}
