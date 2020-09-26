package org.shoulder.crypto.negotiation.support.server;

import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cipher.TransportTextCipher;
import org.shoulder.crypto.negotiation.dto.SensitiveFieldWrapper;
import org.shoulder.crypto.negotiation.support.Sensitive;
import org.shoulder.crypto.negotiation.util.SensitiveFieldCache;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 服务端敏感api接口拦截器
 * 只拦截握手完毕后的加密接口，即只拦截header中带 xSessionId 和 xDk 的请求。
 * order 一般在最早生效，如监控、日志拦截器之后，其他拦截器之前，具体顺序由具体场景决定
 * RequestBodyAdvice仅对使用了@RqestBody注解的生效
 *
 * @author lym
 * @see SensitiveRequestDecryptHandlerInterceptor 解密器在这里创建
 */
@RestControllerAdvice
public class SensitiveRequestDecryptAdvance extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 只拦截带 @Sensitive 的接口
        Method method = methodParameter.getMethod();
        return method != null && method.isAnnotationPresent(Sensitive.class);
    }

    /**
     * 读取请求体参数后执行，解密参数
     */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {

        Class<?> resultClazz = body.getClass();
        TransportTextCipher cipher = TransportCipherHolder.removeRequestCipher();
        List<SensitiveFieldWrapper> securityResultField = SensitiveFieldCache.findSensitiveResponseFieldInfo(resultClazz);
        if (!CollectionUtils.isEmpty(securityResultField)) {
            // 解密
            SensitiveFieldCache.handleSensitiveData(body, securityResultField, cipher);
        }
        return body;
    }


}
