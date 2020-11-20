package org.shoulder.crypto.negotiation.support;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cipher.DefaultTransportCipher;
import org.shoulder.crypto.negotiation.cipher.TransportTextCipher;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedList;

/**
 * 安全的 restTemplate，用于安全传输带私密字段的请求
 * 未适配 kotlin （不带 RequestCallback 参数的 execute 方法）
 * Java 代码使用 rest Template 最终都会经过 3个带 RequestCallback 参数的 execute 方法(General execution)去调 doExecute
 * 创建请求头的部分涉及 {@link RestTemplate#acceptHeaderRequestCallback} 两个方法，因此在 acceptHeaderRequestCallback 进行拦截而不是 doExecute
 *
 * @author lym
 */
public class SecurityRestTemplate extends RestTemplate {

    private static final Logger log = LoggerFactory.getLogger(SecurityRestTemplate.class);

    private final TransportNegotiationService transportNegotiationService;

    private final TransportCryptoUtil cryptoUtil;

    private static final ThreadLocal<URI> URI_LOCAL = new ThreadLocal<>();

    public SecurityRestTemplate(TransportNegotiationService transportNegotiationService, TransportCryptoUtil cryptoUtil) {
        this.transportNegotiationService = transportNegotiationService;
        this.cryptoUtil = cryptoUtil;
    }

    @Override
    @Nullable
    protected <T> T doExecute(URI uri, @Nullable HttpMethod method, @Nullable RequestCallback requestCallback,
                              @Nullable ResponseExtractor<T> responseExtractor) throws RestClientException {
        // 是否为密钥交换接口，如果标记为跳过（为协商 url、协商 param）则不需要握手
        if (transportNegotiationService.isNegotiationUrl(uri)) {
            // 不做任何处理
            return super.doExecute(uri, method, requestCallback, responseExtractor);
        }
        // 确保已经交换密钥，增强
        URI_LOCAL.set(uri);
        T result = super.doExecute(uri, method, requestCallback, responseExtractor);
        URI_LOCAL.remove();
        return result;
    }

    @Nonnull
    @Override
    public <T> RequestCallback httpEntityCallback(@Nullable Object requestBody) {
        return new EnsureNegotiatedRequestCallback(super.httpEntityCallback(requestBody),
            transportNegotiationService, cryptoUtil);
    }

    @Nonnull
    @Override
    public <T> RequestCallback httpEntityCallback(@Nullable Object requestBody, @Nonnull Type responseType) {
        return new EnsureNegotiatedRequestCallback(super.httpEntityCallback(requestBody, responseType),
            transportNegotiationService, cryptoUtil);
    }


    /**
     * 确保完成已经完成密钥协商，并在请求头中添加密钥协商所需参数
     *
     * @author lym
     */
    private static class EnsureNegotiatedRequestCallback implements RequestCallback {
        private RequestCallback delegate;

        private final TransportNegotiationService transportNegotiationService;

        private final TransportCryptoUtil cryptoUtil;

        public EnsureNegotiatedRequestCallback(RequestCallback delegate, TransportNegotiationService transportNegotiationService,
                                               TransportCryptoUtil cryptoUtil) {
            this.delegate = delegate;
            this.transportNegotiationService = transportNegotiationService;
            this.cryptoUtil = cryptoUtil;
        }

        @Override
        public void doWithRequest(@Nonnull ClientHttpRequest request) throws IOException {
            try {
                // 协商密钥并添加需要的请求头
                HttpHeaders headers = negotiateBeforeExecute(URI_LOCAL.get());
                HttpHeaders httpHeaders = request.getHeaders();
                headers.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
            } catch (AesCryptoException | AsymmetricCryptoException e) {
                throw new RestClientException("Negotiate FAIL before doExecute!", e);
            }
            delegate.doWithRequest(request);
        }

        /**
         * 在请求构建发送前，确保已经完成密钥协商
         */
        private HttpHeaders negotiateBeforeExecute(URI uri) throws AesCryptoException, AsymmetricCryptoException {
            int time = 0;
            KeyExchangeResult keyExchangeResult = null;
            while (keyExchangeResult == null) {
                keyExchangeResult = negotiate(uri, time);
                KeyNegotiationCache.CLIENT_LOCAL_CACHE.set(keyExchangeResult);
                time++;
            }

            // 创建本次请求的加密器 todo 【性能】 小优化，如果请求不带（敏感）参数，则无需生成数据密钥 —— 1. 保存 keyChangeResult。2. 如何感知是否要加密
            byte[] requestDk = TransportCryptoUtil.generateDataKey(keyExchangeResult.getKeyLength());
            TransportTextCipher requestEncryptCipher = DefaultTransportCipher.buildEncryptCipher(keyExchangeResult, requestDk);
            TransportCipherHolder.setRequestCipher(requestEncryptCipher);

            return cryptoUtil.generateHeaders(keyExchangeResult, requestDk);

        }

        private KeyExchangeResult negotiate(URI uri, int time) {

            // 限制协商尝试次数（2）。超过抛异常
            final int negotiationMaxTimes = 2;
            if (time >= negotiationMaxTimes) {
                log.error("check secure session exceed the max time(" + negotiationMaxTimes + "), FAIL! uri={}", uri);
                throw new IllegalStateException("check secure session exceed the max time(" + negotiationMaxTimes + "), FAIL! uri=" + uri);
            }

            // 密钥协商
            try {
                return transportNegotiationService.requestForNegotiate(uri);
            } catch (NegotiationException e) {
                log.warn("Try negotiate FAIL with '{}', time({})", uri, time, e);
                return null;
            }
        }
    }

}
