package org.shoulder.web.advice;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.shoulder.web.annotation.SkipResponseWrap;
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
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一接口返回值
 * 自动将 json 或者 str 类型返回值（RestController 的返回值）用 {@link BaseResponse} 包装。
 * 关闭包装：
 * 禁止对某个方法返回值包装： 方法上添加 {@link SkipResponseWrap}
 * 禁止对某个RestController类的所有返回值包装： 类上添加 {@link SkipResponseWrap}
 * 禁用功能： shoulder.web.unionResponse=false
 * <p>
 * 如果希望使用自己项目中的返回值类，返回值继承 {@link BaseResponse} 类即可。
 * <p>
 *
 * @author lym
 */
@RestControllerAdvice
public class RestControllerUnionResponseAdvice implements ResponseBodyAdvice<Object> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 是否需要包装返回值，自动包装必须符合以下条件
     * 接口返回值格式化类型为 Json，且不是 Spring 的标准返回值类型 {@link ResponseEntity}
     * 方法或类上未添加 {@link SkipResponseWrap}
     */
    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {

        boolean jsonType = MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
        boolean stringType = StringHttpMessageConverter.class.isAssignableFrom(converterType);
        boolean notSupportResponseType = !jsonType && !stringType;
        if (notSupportResponseType) {
            // 返回值不是 json对象 且不是字符串类型
            return false;
        }
        boolean springStdResponseType = ResponseEntity.class.isAssignableFrom(returnType.getParameterType());
        if (springStdResponseType) {
            // Spring 框架的返回值
            return false;
        }

        // 方法、或类上不能有 SkipResponseWrap 注解
        boolean withoutMethodAnnotation = returnType.getMethodAnnotation(SkipResponseWrap.class) == null;
        boolean withoutClassAnnotation = AnnotatedElementUtils.findMergedAnnotation(returnType.getContainingClass(),
            SkipResponseWrap.class) == null;
        return withoutMethodAnnotation && withoutClassAnnotation;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {

        if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            if (body == null) {
                log.debug("body is null");
                return BaseResponse.success();
            }
            // json
            if (BaseResponse.class.isAssignableFrom(body.getClass())) {
                return body;
            }
            return BaseResponse.success().setData(body);
        } else {
            // string 类型单独处理
            if (body == null || StringUtils.isEmpty((CharSequence) body)) {
                return "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"\"}";
            } else {
                return "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"" + body + "\"}";
            }

        }

    }

}
