package org.shoulder.crypto.negotiation.support.server;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.support.Sensitive;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 服务端敏感api接口拦截器
 * 只拦截握手完毕后的加密接口，即只拦截header中带 xSessionId 和 xDk 的请求。
 * order 一般在最早生效，如监控、日志拦截器之后，其他拦截器之前，具体顺序由具体场景决定
 * RequestBodyAdvice仅对使用了@RqestBody注解的生效
 *
 * @author lym
 */
public class SensitiveRequestDecryptAdvance extends RequestBodyAdviceAdapter {

    private static final Logger log = LoggerFactory.getLogger(SensitiveRequestDecryptAdvance.class);

    private KeyNegotiationCache keyNegotiationCache;

    private TransportCryptoUtil transportCryptoUtil;

    public SensitiveRequestDecryptAdvance(KeyNegotiationCache keyNegotiationCache, TransportCryptoUtil transportCryptoUtil) {
        this.keyNegotiationCache = keyNegotiationCache;
        this.transportCryptoUtil = transportCryptoUtil;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //if (this.skipInterceptor(request, handler)) {
        //    return true;
        //} else {
        //}
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hMethod = (HandlerMethod) handler;
        Sensitive sensitiveAnnotation = hMethod.getMethod().getAnnotation(Sensitive.class);
        if(sensitiveAnnotation == null){
            // 只拦截带 @Sensitive 的接口
            return true;
        }
        String xSessionId = request.getHeader(KeyExchangeConstants.SECURITY_SESSION_ID);
        String xDk = request.getHeader(KeyExchangeConstants.SECURITY_DATA_KEY);
        String token = request.getHeader(KeyExchangeConstants.TOKEN);
        if(log.isDebugEnabled()){
            // 记录请求中这几个重要地参数，便于排查问题
            log.debug("xSessionId: {}, xDk: {}, token: {}.", xSessionId, xDk, token);
        }
        //if (StringUtils.isEmpty(xSessionId) || StringUtils.isEmpty(xDk) || StringUtils.isEmpty(token)) {
            // xSessionId 没有说明不是一个 ecdh 请求，没有 xDk 说明还在密钥协商阶段
            // return true;
        //}

        // 一、处理请求：解密发送方的会话密钥
        KeyExchangeResult cacheKeyExchangeResult = keyNegotiationCache.getAsServer(xSessionId);

        if (cacheKeyExchangeResult == null) {
            // 返回重新握手错误码
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            BaseResponse r = BaseResponse.error(CommonErrorCodeEnum.SECURITY_SESSION_INVALID);
            response.getWriter().write(JsonUtils.toJson(r));
            return false;
        }
        KeyNegotiationCache.SERVER_LOCAL_CACHE.set(cacheKeyExchangeResult);

        // 校验token是否正确
        transportCryptoUtil.verifyToken(xSessionId, xDk, token);

        log.debug("security request. xDk is " + xDk);
        // 解密数据密钥
        byte[] requestDk = TransportCryptoUtil.decryptDk(cacheKeyExchangeResult, xDk);
        // 缓存请求解密处理器
        TransportCipher requestDecryptCipher = TransportCipher.buildDecryptCipher(cacheKeyExchangeResult, requestDk);
        TransportCipherHolder.setRequestCipher(requestDecryptCipher);

        // todo 解密请求

        /*Object result = super.read(type, contextClass, inputMessage);
        Object toCrypt = result;
        // 专门处理 BaseResponse 以及其子类
        if (result instanceof BaseResponse) {
            toCrypt = ((BaseResponse) toCrypt).getData();
        }
        if (toCrypt == null) {
            return result;
        }
        Class<?> resultClazz = toCrypt.getClass();
        TransportCipher cipher = TransportCipherHolder.removeResponseCipher();
        List<SensitiveFieldWrapper> securityResultField = SensitiveFieldCache.findSensitiveResponseFieldInfo(resultClazz);
        if (!CollectionUtils.isEmpty(securityResultField)) {
            // 解密
            SensitiveFieldCache.handleSensitiveData(toCrypt, securityResultField, cipher);
        }
        return result;*/

        return true;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 只拦截带 @Sensitive 的接口
        Method method = methodParameter.getMethod();
        return method != null && method.isAnnotationPresent(Sensitive.class);
    }

    /**
     * 读取参数前执行
     * 在此做些编码 / 解密 / 封装参数为对象的操作
     */
    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        //return new MappingJacksonInputMessage();
        return inputMessage;
    }

    /**
     * 读取请求体参数后执行
     */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    /**
     * 无请求体时的处理
     */
    @Nullable
    @Override
    public Object handleEmptyBody(@Nullable Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }


}
