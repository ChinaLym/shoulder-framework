package org.shoulder.crypto.negotiation.interceptor;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 自动加密响应，注意不要与统一拦截器顺序冲突
 *
 * @author lym
 */
@RestControllerAdvice
public class SecurityRestControllerAutoCryptoResponseAdvice implements ResponseBodyAdvice<Object> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {

        boolean jsonType = MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
        // 返回值不是 json对象或为 Spring 框架的返回值
        return jsonType && !ResponseEntity.class.isAssignableFrom(returnType.getParameterType());
    }

    /**
     * 加密响应，抛出异常时，Spring 不会执行该方法
     */
    @Override
    public Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {

        if (body == null) {
            // 不应该出现的情况
            log.debug("body is null");
            return null;
        }
        // json
        if (body instanceof BaseResponse) {
            Object realData = ((BaseResponse) body).getData();
            return body;
        }
        return body;

    }

}
