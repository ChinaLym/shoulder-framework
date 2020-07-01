package org.shoulder.crypto.negotiation.interceptor;

import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;
import org.shoulder.crypto.negotiation.cache.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * RestTemplate拦截器。
 * client 向 server 发出安全请求，响应自动解密
 * 用于 client 端解密目标服务返回的加密信息。
 *
 * todo 由于仅处理响应后，依赖发送上下文，处理强制握手返回错误码，考虑塞进 SecurityRestTemplate？
 *
 * @author lym
 */
@Component
public class ExchangeKeyHttpClientInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeKeyHttpClientInterceptor.class);


    private KeyNegotiationCache keyNegotiationCache;

    private TransportCryptoUtil transportCryptoUtil;

    public ExchangeKeyHttpClientInterceptor(KeyNegotiationCache keyNegotiationCache, TransportCryptoUtil transportCryptoUtil) {
        this.keyNegotiationCache = keyNegotiationCache;
        this.transportCryptoUtil = transportCryptoUtil;
    }

    @Override
    public ClientHttpResponse intercept( HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // *************************** preRequest ***************************

        ClientHttpResponse response = execution.execute(request, body);

        // *************************** afterRequest ***************************
        if(response.getStatusCode() != HttpStatus.OK){
            // todo 校验错误码，是否为强制重新进行密钥协商
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

            // 2. 获取本次请求真正的数据密钥 THREAD_LOCAL
            KeyExchangeResult keyExchangeInfo = KeyNegotiationCache.THREAD_LOCAL.get();
            byte[] realDataKey = TransportCryptoUtil.decryptDk(keyExchangeInfo, xDk);

            // 3. 放置于线程变量中供后续解密使用
            TransportCipher responseDecryptor = TransportCipher.decryptor(keyExchangeInfo, realDataKey);
            TransportCipherHolder.setResponseDecryptor(responseDecryptor);

        } catch (AsymmetricCryptoException e) {
            throw new RuntimeException("security token validate fail!");
        } catch (SymmetricCryptoException e) {
            logger.warn("Decrypt xDk has a error in ExchangeKeyRPCInterceptor!", e);
        } finally {
            // 清理线程变量
            KeyNegotiationCache.THREAD_LOCAL.remove();
        }
        return response;
    }

}
