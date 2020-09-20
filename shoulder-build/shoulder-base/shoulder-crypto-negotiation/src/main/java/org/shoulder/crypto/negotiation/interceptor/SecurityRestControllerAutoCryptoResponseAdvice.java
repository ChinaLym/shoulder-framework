package org.shoulder.crypto.negotiation.interceptor;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;
import org.shoulder.crypto.negotiation.cache.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
 * 服务端敏感api响应自动加密，注意不要与统一拦截器顺序冲突
 *
 * @author lym
 */
@RestControllerAdvice
public class SecurityRestControllerAutoCryptoResponseAdvice implements ResponseBodyAdvice<Object> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final KeyNegotiationCache keyNegotiationCache;

    private final TransportCryptoUtil transportCryptoUtil;

    public SecurityRestControllerAutoCryptoResponseAdvice(KeyNegotiationCache keyNegotiationCache, TransportCryptoUtil transportCryptoUtil) {
        this.keyNegotiationCache = keyNegotiationCache;
        this.transportCryptoUtil = transportCryptoUtil;
    }

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
        Object toEncryptDTO = body;
        // json
        if (body instanceof BaseResponse) {
            toEncryptDTO = ((BaseResponse) body).getData();
            if (toEncryptDTO == null) {
                return body;
            }
        }

        HttpHeaders requestHeaders = request.getHeaders();
        String xSessionId = requestHeaders.getFirst(KeyExchangeConstants.SECURITY_SESSION_ID);

        // 生成返回值加密的数据密钥，以加密要返回的敏感数据信息（请求和响应中使用的数据密钥不同）
        KeyExchangeResult cacheKeyExchangeResult = keyNegotiationCache.getAsServer(xSessionId);
        if (cacheKeyExchangeResult == null) {
            // todo 处理接口请求时过期，导致本次接口失败。应该提前缓存到线程变量中，避免这种情况发生。目前暂时返回重新握手错误码，让客户端重新发一次请求

            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
            return BaseResponse.error(CommonErrorCodeEnum.AUTH_401_EXPIRED);
        }
        try {
            byte[] responseDk = TransportCryptoUtil.generateDataKey(cacheKeyExchangeResult.getKeyLength());
            // 缓存响应加密处理器
            TransportCipher responseEncryptCipher = TransportCipher.buildEncryptCipher(cacheKeyExchangeResult, responseDk);
            TransportCipherHolder.setResponseCipher(responseEncryptCipher);
            String responseX_Dk = TransportCryptoUtil.encryptDk(cacheKeyExchangeResult, responseDk);
            log.debug("security response. xDk is " + responseX_Dk);
            // todo

            HttpHeaders responseHeaders = request.getHeaders();
            responseHeaders.add("Token", transportCryptoUtil.generateToken(xSessionId, responseX_Dk));
            responseHeaders.add("xSessionId", cacheKeyExchangeResult.getxSessionId());
            responseHeaders.add("xDk", responseX_Dk);
        } catch (AsymmetricCryptoException e) {
            log.warn("token generate fail!", e);
            throw new RuntimeException("token generate fail!", e);
        } catch (SymmetricCryptoException e) {
            log.warn("encrypt dk fail!", e);
            throw new RuntimeException("encrypt dk fail!", e);
        } finally {
            // 清理线程变量
            KeyNegotiationCache.THREAD_LOCAL.remove();
        }
        // 加密 toEncryptDTO
        return body;
    }

}
