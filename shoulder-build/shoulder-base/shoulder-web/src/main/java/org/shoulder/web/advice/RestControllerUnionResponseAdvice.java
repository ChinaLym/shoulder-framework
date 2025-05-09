package org.shoulder.web.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.ArrayUtils;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.ServletUtil;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 统一接口返回值
 * 自动将 json 或者 str 类型返回值（RestController 的返回值）用 {@link BaseResult} 包装。
 * 关闭包装：
 * 禁止对某个方法返回值包装： 方法上添加 {@link SkipResponseWrap}
 * 禁止对某个RestController类的所有返回值包装： 类上添加 {@link SkipResponseWrap}
 * 禁用功能： shoulder.web.restResponse=false
 * <p>
 * 如果希望使用自己项目中的返回值类，返回值继承 {@link BaseResult} 类即可。
 * <p>
 *
 * @author lym
 */
@RestControllerAdvice
public class RestControllerUnionResponseAdvice implements ResponseBodyAdvice<Object> {

    private final Logger log = ShoulderLoggers.SHOULDER_WEB;

    private final PathMatcher matcher = new AntPathMatcher();

    private final List<String> skipWarpPathPatterns;

    public RestControllerUnionResponseAdvice(List<String> skipWarpPathPatterns) {
        this.skipWarpPathPatterns = skipWarpPathPatterns;
    }

    private final String errorResponse = "{\"code\":\"" + CommonErrorCodeEnum.UNKNOWN.getCode() + "\",\"msg\":\"RestControllerUnionResponseAdvice write FAIL!\",\"data\":\"\"}";

    /**
     * 是否需要包装返回值，自动包装必须符合以下条件
     * 接口返回值格式化类型为 Json，且不是 Spring 的标准返回值类型 {@link ResponseEntity}
     * 方法或类上未添加 {@link SkipResponseWrap}
     */
    @Override
    public boolean supports(@Nonnull MethodParameter returnType,
                            @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {

        boolean jsonType = MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
        boolean stringType = StringHttpMessageConverter.class.isAssignableFrom(converterType);
        boolean notSupportResponseType = !jsonType && !stringType;
        if (notSupportResponseType) {
            // 返回值不是 json对象 且不是字符串类型
            return false;
        }
        Class<?> returnClass = returnType.getParameterType();
        if (
            // 已经是 shoulder 标准返回
            BaseResult.class.isAssignableFrom(returnClass)
                // Spring 框架的返回值，这类往往有特殊诉求
                || ResponseEntity.class.isAssignableFrom(returnClass)
                // void 类型，无需返回
                || Void.class.isAssignableFrom(returnClass) || void.class.isAssignableFrom(returnClass)) {

            return false;
        }

        // 配置文件里明确指出不要包装
        if (CollectionUtils.isNotEmpty(skipWarpPathPatterns)) {
            String requestPath = ServletUtil.getRequest().getRequestURI();
            for (String unWarpPathPattern : skipWarpPathPatterns) {
                if (matcher.match(unWarpPathPattern, requestPath)) {
                    return false;
                }
            }
        }

        // 方法、或类上不能有 SkipResponseWrap 注解
        boolean withoutMethodAnnotation = returnType.getMethodAnnotation(SkipResponseWrap.class) == null;
        boolean withoutClassAnnotation = AnnotatedElementUtils.findMergedAnnotation(returnType.getContainingClass(),
            SkipResponseWrap.class) == null;
        boolean restController = AnnotatedElementUtils.findMergedAnnotation(returnType.getContainingClass(),
            RestController.class) != null;
        boolean responseBody = AnnotatedElementUtils.findMergedAnnotation(returnType.getContainingClass(),
            ResponseBody.class) != null;
        boolean hasSpecialProduces = Optional.ofNullable(returnType.getMethodAnnotation(RequestMapping.class))
            .map(RequestMapping::produces)
            .filter(ArrayUtils::isNotEmpty)
            .map(StringUtils::isNoneBlank)
            .orElse(false);
        return !hasSpecialProduces && withoutMethodAnnotation && withoutClassAnnotation && (restController || responseBody);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object beforeBodyWrite(@Nullable Object body, @Nonnull MethodParameter returnType,
                                  @Nonnull MediaType selectedContentType,
                                  @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @Nonnull ServerHttpRequest request,
                                  @Nonnull ServerHttpResponse response) {

        if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            if (body == null) {
                log.debug("body is null");
                return BaseResult.success();
            } else if (body instanceof Collection) {
                return ListResult.of((Collection) body);
            }
            return BaseResult.success().setData(body);
        } else {
            // string 类型单独处理
            CharSequence bodyStr = (CharSequence) body;
            if (bodyStr == null) {
                return  "{\"code\":\"0\",\"msg\":\"success\",\"data\":null}";
            } else if(bodyStr.isEmpty()) {
                return "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"\"}";
            } else {
                try {
                    return JsonUtils.getInstance().writeValueAsString(BaseResult.success().setData(body));
                } catch (JsonProcessingException e) {
                    log.error(CommonErrorCodeEnum.UNKNOWN, e);
                    return errorResponse;
                }
            }

        }

    }

}
