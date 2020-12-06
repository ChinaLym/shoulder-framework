package org.shoulder.crypto.negotiation.support.client;

import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.NegotiationCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cipher.DefaultTransportCipher;
import org.shoulder.crypto.negotiation.cipher.TransportTextCipher;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.support.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.shoulder.http.AppIdExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * RestTemplate拦截器。
 * client 向 server 发出安全请求，在响应后解析响应头，将解密器放置于线程变量中
 * <p>
 *
 * @author lym
 * @see SecurityRestTemplate
 */
public class SensitiveResponseDecryptInterceptor implements ClientHttpRequestInterceptor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(SensitiveResponseDecryptInterceptor.class);

    private final TransportCryptoUtil transportCryptoUtil;

    private final NegotiationCache negotiationCache;

    private final AppIdExtractor appIdExtractor;


    public SensitiveResponseDecryptInterceptor(TransportCryptoUtil transportCryptoUtil,
                                               NegotiationCache negotiationCache, AppIdExtractor appIdExtractor) {
        this.transportCryptoUtil = transportCryptoUtil;
        this.negotiationCache = negotiationCache;
        this.appIdExtractor = appIdExtractor;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] requestBody, ClientHttpRequestExecution execution) throws IOException {
        // *************************** preRequest ***************************

        ClientHttpResponse response = execution.execute(request, requestBody);

        // *************************** afterRequest ***************************
        // 若包含协商缓存无效 / 过期的标记，则清理缓存（服务提供方发生重启等导致密钥缓存提前过期）注意，应该要支持多种方式判断，如多个错误码，响应 httpCode 等
        List<String> negotiationInvalidHeader = response.getHeaders().get(NegotiationConstants.NEGOTIATION_INVALID_TAG);
        if (!CollectionUtils.isEmpty(negotiationInvalidHeader)) {
            // if (negotiationInvalidHeader.contains(NegotiationErrorCodeEnum.NEGOTIATION_INVALID.getCode())) {
            String aimServiceAppId = appIdExtractor.extract(request.getURI());
            negotiationCache.delete(aimServiceAppId, true);
            NegotiationCache.CLIENT_LOCAL_CACHE.remove();
            log.warn("sensitive request FAIL for response with a invalid negotiation(xSessionId) mark, clean the negotiation cache.");
            // } else {
            // 对方未遵守约定，只返回了标记，未返回错误码
            //     log.warn("invalid response");
            // }
        }

        HttpHeaders headers = response.getHeaders();
        String token = headers.getFirst(NegotiationConstants.TOKEN);
        String xSessionId = headers.getFirst(NegotiationConstants.SECURITY_SESSION_ID);
        String xDk = headers.getFirst(NegotiationConstants.SECURITY_DATA_KEY);

        // 只对标志为加密的响应拦截
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(xSessionId) || StringUtils.isEmpty(xDk)) {
            return response;
        }

        // 确定为加密的响应拦截
        // 1. 验证服务端签名
        try {
            if (!transportCryptoUtil.verifyToken(xSessionId, xDk, token, NegotiationCache.CLIENT_LOCAL_CACHE.get().getPublicKey())) {
                throw new RuntimeException("security token validate fail!");
            }

            // 2. 获取本次请求真正的数据密钥
            NegotiationResult keyExchangeInfo = NegotiationCache.CLIENT_LOCAL_CACHE.get();
            if (keyExchangeInfo == null) {
                throw new IllegalStateException("keyExchangeInfo can't be null!");
            }
            byte[] realDataKey = TransportCryptoUtil.decryptDk(keyExchangeInfo, xDk);

            TransportTextCipher responseDecryptCipher = DefaultTransportCipher.buildDecryptCipher(keyExchangeInfo, realDataKey);
            TransportCipherHolder.setResponseCipher(responseDecryptCipher);

        } catch (AsymmetricCryptoException e) {
            log.warn("token validate fail!", e);
            throw new RuntimeException("token validate fail!", e);
        } catch (SymmetricCryptoException e) {
            log.warn("Decrypt xDk fail!", e);
            throw new RuntimeException("Decrypt xDk fail!", e);
        } finally {
            // 清理线程变量
            NegotiationCache.CLIENT_LOCAL_CACHE.remove();
        }
        return response;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30;
    }
}
