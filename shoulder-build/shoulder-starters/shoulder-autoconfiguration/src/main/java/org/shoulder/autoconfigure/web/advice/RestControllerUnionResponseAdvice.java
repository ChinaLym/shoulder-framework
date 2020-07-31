package org.shoulder.autoconfigure.web.advice;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.util.StringUtils;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一接口返回值
 * 自动将 json 或者 str 类型返回值（RestController 的返回值）用 {@link BaseResponse} 包装。
 *      关闭包装：
 *          禁止对某个方法返回值包装： 方法上添加 {@link SkipResponseWrap}
 *          禁止对某个RestController类的所有返回值包装： 类上添加 {@link SkipResponseWrap}
 *          禁用功能： shoulder.web.unionResponse=false
 *
 *      如果希望使用自己项目中的返回值类，返回值继承 {@link BaseResponse} 类即可。
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
	    if(MappingJackson2HttpMessageConverter.class.isAssignableFrom(selectedConverterType)){
	        // json
            if (body == null) {
                return BaseResponse.success();
            }
            if (BaseResponse.class.isAssignableFrom(body.getClass())) {
                return body;
            }
            return BaseResponse.success().setData(body);
        } else {
	        // string
            if(StringUtils.isEmpty((CharSequence) body)){
                return "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"\"}";
            }else {
                return "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"" + body + "\"}";
            }

        }

	}

    /**
     * 是否需要包装返回值，自动包装必须符合以下条件
     *      接口返回值格式化类型为 Json
     *      不是 Spring 的标准返回值类型 {@link ResponseEntity}
     *      方法或类上未添加 {@link SkipResponseWrap}
     */
    private boolean needWrapResponse(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        boolean jsonType = MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
        boolean stringType = StringHttpMessageConverter.class.isAssignableFrom(converterType);
        boolean supportToJson = jsonType || stringType;
        boolean springStdResponseType = ResponseEntity.class.isAssignableFrom(returnType.getParameterType());
        if (!supportToJson || springStdResponseType){
            return false;
        }

        boolean withoutMethodAnnotation = returnType.getMethodAnnotation(SkipResponseWrap.class) == null;
        boolean withoutClassAnnotation = AnnotatedElementUtils.findMergedAnnotation(returnType.getContainingClass(),
            SkipResponseWrap.class) == null;
        return withoutMethodAnnotation && withoutClassAnnotation;

    }

}
