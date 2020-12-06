package org.shoulder.crypto.negotiation.support.server;

import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.NegotiationCache;
import org.shoulder.crypto.negotiation.cipher.DefaultTransportCipher;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.dto.SensitiveFieldWrapper;
import org.shoulder.crypto.negotiation.exception.NegotiationErrorCodeEnum;
import org.shoulder.crypto.negotiation.support.Sensitive;
import org.shoulder.crypto.negotiation.util.SensitiveFieldCache;
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
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 服务端敏感api响应自动加密，注意不要与统一返回值格式包装器顺序冲突
 *
 * @author lym
 */
@RestControllerAdvice
public class SensitiveResponseEncryptAdvice implements ResponseBodyAdvice<Object> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final TransportCryptoUtil transportCryptoUtil;

    public SensitiveResponseEncryptAdvice(TransportCryptoUtil transportCryptoUtil) {
        this.transportCryptoUtil = transportCryptoUtil;
    }

    @Override
    public boolean supports(@Nonnull MethodParameter returnType,
                            @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {

        boolean jsonType = MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
        // 返回值不是 json对象或为 Spring 框架的返回值
        if (!jsonType || ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // Controller 方法上必须要有 @Sensitive 注解
        Sensitive methodAnnotation = returnType.getMethodAnnotation(Sensitive.class);
        return methodAnnotation != null;
    }

    /**
     * 加密响应，抛出异常时，Spring 不会执行该方法
     */
    @Override
    public Object beforeBodyWrite(@Nullable Object body, @Nonnull MethodParameter returnType,
                                  @Nonnull MediaType selectedContentType,
                                  @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @Nonnull ServerHttpRequest request,
                                  @Nonnull ServerHttpResponse response) {

        if (body == null) {
            // 不应该出现的情况
            log.debug("body is null");
            return null;
        }
        Object toEncryptDTO = body;
        // json
        if (body instanceof RestResult) {
            toEncryptDTO = ((RestResult) body).getData();
            if (toEncryptDTO == null) {
                return body;
            }
        }
        List<SensitiveFieldWrapper> sensitiveFieldWrapperList =
            SensitiveFieldCache.findSensitiveResponseFieldInfo(toEncryptDTO.getClass());
        if (CollectionUtils.isEmpty(sensitiveFieldWrapperList)) {
            return body;
        }

        HttpHeaders requestHeaders = request.getHeaders();
        String xSessionId = requestHeaders.getFirst(NegotiationConstants.SECURITY_SESSION_ID);
        if (xSessionId == null) {
            // 非加密接口
            return body;
        }

        // 生成返回值加密的数据密钥，以加密要返回的敏感数据信息（请求和响应中使用的数据密钥不同）
        NegotiationResult cacheNegotiationResult = NegotiationCache.SERVER_LOCAL_CACHE.get();
        // 若在接口响应时过期，会导致本次接口失败。这里使用线程变量中的，规避处理过程中握手信息过期，同理客户端也是使用发出请求那一刻的密钥
        if (cacheNegotiationResult == null) {
            // 按理说不会为 null，因为这里读取是时线程变量
            response.getHeaders().set(NegotiationConstants.NEGOTIATION_INVALID_TAG, NegotiationErrorCodeEnum.NEGOTIATION_INVALID.getCode());
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
            return RestResult.error(NegotiationErrorCodeEnum.NEGOTIATION_INVALID);
        }
        try {
            byte[] responseDk = TransportCryptoUtil.generateDataKey(cacheNegotiationResult.getKeyLength());
            // 缓存响应加密处理器
            DefaultTransportCipher responseEncryptCipher = DefaultTransportCipher.buildEncryptCipher(cacheNegotiationResult, responseDk);
            String responseX_Dk = TransportCryptoUtil.encryptDk(cacheNegotiationResult, responseDk);
            log.debug("security response. xDk is " + responseX_Dk);
            //  加密 toEncryptDTO
            SensitiveFieldCache.handleSensitiveData(toEncryptDTO, sensitiveFieldWrapperList, responseEncryptCipher);

            HttpHeaders responseHeaders = response.getHeaders();
            responseHeaders.add("Token", transportCryptoUtil.generateToken(xSessionId, responseX_Dk));
            responseHeaders.add("xSessionId", cacheNegotiationResult.getxSessionId());
            responseHeaders.add("xDk", responseX_Dk);
        } catch (AsymmetricCryptoException e) {
            log.warn("token generate fail!", e);
            throw new RuntimeException("token generate fail!", e);
        } catch (SymmetricCryptoException e) {
            log.warn("encrypt dk fail!", e);
            throw new RuntimeException("encrypt dk fail!", e);
        } finally {
            // 清理线程变量
            NegotiationCache.SERVER_LOCAL_CACHE.remove();
        }
        return body;
    }

}
