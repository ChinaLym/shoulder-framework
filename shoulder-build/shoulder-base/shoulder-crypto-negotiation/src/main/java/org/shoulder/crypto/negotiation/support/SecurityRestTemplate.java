package org.shoulder.crypto.negotiation.support;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.NegotiationResultCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cipher.DefaultTransportCipher;
import org.shoulder.crypto.negotiation.cipher.TransportTextCipher;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.shoulder.crypto.symmetric.exception.SymmetricCryptoException;
import org.shoulder.http.AppIdExtractor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * 安全的 restTemplate，用于安全传输带私密字段的请求
 * 未适配 kotlin （不带 RequestCallback 参数的 execute 方法）
 * Java 代码使用 rest Template 最终都会经过 3个带 RequestCallback 参数的 execute 方法(General execution)去调 doExecute
 * 创建请求头的部分涉及 {@link RestTemplate#acceptHeaderRequestCallback} 两个方法，因此在 acceptHeaderRequestCallback 进行拦截而不是 doExecute
 *
 * @author lym
 */
public class SecurityRestTemplate extends RestTemplate {

    private static final Logger log = ShoulderLoggers.SHOULDER_CLIENT;

    private static final ThreadLocal<URI> URI_LOCAL = new ThreadLocal<>();

    private final TransportNegotiationService transportNegotiationService;

    private final TransportCryptoUtil cryptoUtil;

    private final NegotiationResultCache negotiationResultCache;

    private final AppIdExtractor appIdExtractor;

    public SecurityRestTemplate(TransportNegotiationService transportNegotiationService, TransportCryptoUtil cryptoUtil, NegotiationResultCache negotiationResultCache, AppIdExtractor appIdExtractor) {
        this.transportNegotiationService = transportNegotiationService;
        this.cryptoUtil = cryptoUtil;
        this.negotiationResultCache = negotiationResultCache;
        this.appIdExtractor = appIdExtractor;
    }

    @Override
    @Nullable
    protected <T> T doExecute(@NonNull URI uri, @Nullable String uriTemplate, @Nullable HttpMethod method, @Nullable RequestCallback requestCallback,
                              @Nullable ResponseExtractor<T> responseExtractor) throws RestClientException {
        // 是否为密钥交换接口，如果标记为跳过（为协商 url、协商 param）则不需要握手
        if (transportNegotiationService.isNegotiationUrl(uri)) {
            // 不做任何处理
            return super.doExecute(uri, uriTemplate, method, requestCallback, responseExtractor);
        }
        // 确保已经交换密钥，增强
        URI_LOCAL.set(uri);
        T result = super.doExecute(uri, uriTemplate, method, requestCallback, responseExtractor);
        if (result instanceof ResponseEntity) {
            HttpHeaders responseHeaders = ((ResponseEntity) result).getHeaders();
            List<String> negotiationInvalidHeader = responseHeaders.get(NegotiationConstants.NEGOTIATION_INVALID_TAG);
            if (!CollectionUtils.isEmpty(negotiationInvalidHeader)) {
                // 若包含协商缓存无效 / 过期的标记，则清理缓存，并重新发送请求
                //negotiationInvalidHeader.contains(NegotiationErrorCodeEnum.NEGOTIATION_INVALID.getCode());
                String aimServiceAppId = appIdExtractor.extract(uri);
                // 仅删除密钥交换缓存即可，因为加密器缓存已经在请求前清理。SensitiveRequestDecryptHandlerInterceptor 中有删除，这里也执行，因为删除是幂等的
                negotiationResultCache.delete(aimServiceAppId, true);
                NegotiationResultCache.CLIENT_LOCAL_CACHE.remove();
                NegotiationResultCache.CLIENT_LOCAL_CACHE.remove();
                log.info("sensitive request FAIL for response with a invalid negotiation(xSessionId) mark, negotiate and retry once.");
                // 重新执行一次即可，此时已经将密钥交换缓存删除，将发起密钥交换
                result = super.doExecute(uri, uriTemplate, method, requestCallback, responseExtractor);
            }
        }
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
        private final TransportNegotiationService transportNegotiationService;
        private final TransportCryptoUtil cryptoUtil;
        private RequestCallback delegate;

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
                URI currentUri = URI_LOCAL.get();
                AssertUtils.notNull(currentUri, CommonErrorCodeEnum.CODING);
                HttpHeaders headers = negotiateBeforeExecute(currentUri);
                HttpHeaders httpHeaders = request.getHeaders();
                headers.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
            } catch (SymmetricCryptoException | AsymmetricCryptoException e) {
                throw new RestClientException("Negotiate FAIL before doExecute!", e);
            }
            delegate.doWithRequest(request);
        }

        /**
         * 在请求构建发送前，确保已经完成密钥协商
         */
        private HttpHeaders negotiateBeforeExecute(URI uri) throws SymmetricCryptoException, AsymmetricCryptoException {
            int time = 0;
            NegotiationResult negotiationResult = null;
            while (negotiationResult == null) {
                negotiationResult = negotiate(uri, time);
                NegotiationResultCache.CLIENT_LOCAL_CACHE.set(negotiationResult);
                time++;
            }

            // 创建本次请求的加密器
            byte[] requestDk = TransportCryptoUtil.generateDataKey(negotiationResult.getKeyLength());
            TransportTextCipher requestEncryptCipher = DefaultTransportCipher.buildEncryptCipher(negotiationResult, requestDk);
            TransportCipherHolder.setRequestCipher(requestEncryptCipher);

            return cryptoUtil.generateHeaders(negotiationResult, requestDk);

        }

        private NegotiationResult negotiate(URI uri, int time) {

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
