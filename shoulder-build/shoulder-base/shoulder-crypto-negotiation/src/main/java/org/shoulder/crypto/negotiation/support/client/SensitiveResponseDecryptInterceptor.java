package org.shoulder.crypto.negotiation.support.client;

import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cipher.DefaultTransportCipher;
import org.shoulder.crypto.negotiation.cipher.TransportTextCipher;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.support.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;

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

    private TransportCryptoUtil transportCryptoUtil;

    public SensitiveResponseDecryptInterceptor(TransportCryptoUtil transportCryptoUtil) {
        this.transportCryptoUtil = transportCryptoUtil;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] requestBody, ClientHttpRequestExecution execution) throws IOException {
        // *************************** preRequest ***************************

        ClientHttpResponse response = execution.execute(request, requestBody);

        // *************************** afterRequest ***************************
        if (response.getStatusCode() != HttpStatus.OK) {
            // todo 【健壮性】校验错误码，是否为协商的密钥过期（在使用框架时，按理说仅发生在服务提供方密钥缓存在内存，且发送重启等导致提前过期）
            log.warn("sensitive request FAIL, responseStatus:" + response.getStatusText());
        }

        HttpHeaders headers = response.getHeaders();
        String token = headers.getFirst(KeyExchangeConstants.TOKEN);
        String xSessionId = headers.getFirst(KeyExchangeConstants.SECURITY_SESSION_ID);
        String xDk = headers.getFirst(KeyExchangeConstants.SECURITY_DATA_KEY);

        // 只对标志为加密的响应拦截
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(xSessionId) || StringUtils.isEmpty(xDk)) {
            return response;
        }

        // 确定为加密的响应拦截
        // 1. 验证服务端签名
        try {
            if (!transportCryptoUtil.verifyToken(xSessionId, xDk, token)) {
                throw new RuntimeException("security token validate fail!");
            }

            // 2. 获取本次请求真正的数据密钥
            KeyExchangeResult keyExchangeInfo = KeyNegotiationCache.CLIENT_LOCAL_CACHE.get();
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
            KeyNegotiationCache.CLIENT_LOCAL_CACHE.remove();
        }
        return response;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30;
    }
}
