package org.shoulder.crypto.negotiation.http;

import lombok.extern.slf4j.Slf4j;
import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;
import org.shoulder.crypto.negotiation.cache.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.shoulder.http.AppIdExtractor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 安全的 restTemplate，用于安全传输带私密字段的请求
 * 未适配 kotlin （不带 RequestCallback 参数的 execute 方法）
 * Java 代码使用 rest Template 最终都会经过 3个带 RequestCallback 参数的 execute 方法(General execution)去调 doExecute
 * 创建请求头的部分涉及 {@link RestTemplate#acceptHeaderRequestCallback} 两个方法，因此在 acceptHeaderRequestCallback 进行拦截而不是 doExecute
 *
 * @author lym
 */
@Slf4j
public class SecurityRestTemplate extends RestTemplate {

    private final TransportNegotiationService transportNegotiationService;

    private final TransportCryptoUtil cryptoUtil;

    private final AppIdExtractor appIdExtractor;

    public SecurityRestTemplate(TransportNegotiationService transportNegotiationService, TransportCryptoUtil cryptoUtil, AppIdExtractor appIdExtractor) {
        this.transportNegotiationService = transportNegotiationService;
        this.cryptoUtil = cryptoUtil;
        this.appIdExtractor = appIdExtractor;
    }


    /**
     * Return a {@code RequestCallback} implementation that writes the given
     * object to the request stream.
     */
    @Override
    public <T> RequestCallback httpEntityCallback(@Nullable Object requestBody) {
        return this.httpEntityCallback(requestBody, null);
    }

    /**
     * Return a {@code RequestCallback} implementation that:
     * <ol>
     * <li>Sets the request {@code Accept} header based on the given response
     * type, cross-checked against the configured message converters.
     * <li>Writes the given object to the request stream.
     * </ol>
     * @see HttpEntityRequestCallback
     */
    @Override
    public <T> RequestCallback httpEntityCallback(@Nullable Object requestBody, Type responseType) {
        return new SecuritySessionRequestCallback(requestBody, responseType);
    }


    // ******************************* RequestCallback *******************************

    /**
     *
     * 继承 spring 的{@link HttpEntityRequestCallback}，在其基础上
     */
    protected class SecuritySessionRequestCallback extends SecurityRestTemplate.HttpEntityRequestCallback {

        public SecuritySessionRequestCallback(Object requestBody, Type responseType) {
            super(requestBody, responseType);
        }

        @Override
        public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
            super.doWithRequest(httpRequest);
            // todo 是否带或可能接收敏感信息？如果不带直接返回
            URI uri = httpRequest.getURI();
            try {
                // 协商密钥并添加需要的请求头
                HttpHeaders headers = negotiateBeforeExecute(uri);
                HttpHeaders httpHeaders = httpRequest.getHeaders();
                headers.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
            } catch (AesCryptoException | AsymmetricCryptoException e) {
                throw new RestClientException("Negotiate FAIL before doExecute!", e);
            }
        }

        /**
         * 在请求构建发送前，确保已经完成密钥协商
         */
        private HttpHeaders negotiateBeforeExecute(URI uri) throws AesCryptoException, AsymmetricCryptoException {
            // 根据 url 获取应用标识，从配置项中获取协商 url，没有则抛异常
            String appId = appIdExtractor.extract(uri);

            int time = 0;
            KeyExchangeResult keyExchangeResult = null;
            while (keyExchangeResult == null) {
                keyExchangeResult = negotiate(appId, time);
                time++;
            }

            // 创建本次请求的加密器 todo 小优化，如果请求不带（敏感）参数，则无需生成数据密钥
            byte[] requestDk = TransportCryptoUtil.generateDataKey(keyExchangeResult.getKeyLength());
            TransportCipher requestEncrypt = TransportCipher.encryptor(keyExchangeResult, requestDk);
            TransportCipherHolder.setRequestEncryptor(requestEncrypt);

            return cryptoUtil.generateHeaders(keyExchangeResult, requestDk);

        }

        private KeyExchangeResult negotiate(String appId, int time) {

            // 限制协商尝试次数（2）。超过抛异常
            final int negotiationMaxTimes = 2;
            if (time >= negotiationMaxTimes) {
                log.error("check secure session exceed the max time(" + negotiationMaxTimes + "), FAIL! serverId={}", appId);
                throw new IllegalStateException("check secure session exceed the max time(" + negotiationMaxTimes + "), FAIL! serverId=" + appId);
            }

            // 密钥协商
            try {
                return transportNegotiationService.requestForNegotiate(appId);
            } catch (NegotiationException e) {
                log.warn("Try negotiate FAIL with {}, time({})", appId, time, e);
                return null;
            }
        }

    }


    // ============================== 以下内容拷贝自 spring RestTemplate ==============================
    // 其设计的还不够灵活，但 spring5 表示不再为 RestTemplate 做功能支持，既然不接 issue，只能手动复制出来了

    /**
     * Request callback implementation that writes the given object to the request stream.
     */
    private class HttpEntityRequestCallback extends SecurityRestTemplate.AcceptHeaderRequestCallback {

        private final HttpEntity<?> requestEntity;

        public HttpEntityRequestCallback(@Nullable Object requestBody) {
            this(requestBody, null);
        }

        public HttpEntityRequestCallback(@Nullable Object requestBody, @Nullable Type responseType) {
            super(responseType);
            if (requestBody instanceof HttpEntity) {
                this.requestEntity = (HttpEntity<?>) requestBody;
            } else if (requestBody != null) {
                this.requestEntity = new HttpEntity<>(requestBody);
            } else {
                this.requestEntity = HttpEntity.EMPTY;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
            super.doWithRequest(httpRequest);
            Object requestBody = this.requestEntity.getBody();
            if (requestBody == null) {
                HttpHeaders httpHeaders = httpRequest.getHeaders();
                HttpHeaders requestHeaders = this.requestEntity.getHeaders();
                if (!requestHeaders.isEmpty()) {
                    requestHeaders.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
                }
                if (httpHeaders.getContentLength() < 0) {
                    httpHeaders.setContentLength(0L);
                }
            } else {
                Class<?> requestBodyClass = requestBody.getClass();
                Type requestBodyType = (this.requestEntity instanceof RequestEntity ?
                    ((RequestEntity<?>) this.requestEntity).getType() : requestBodyClass);
                HttpHeaders httpHeaders = httpRequest.getHeaders();
                HttpHeaders requestHeaders = this.requestEntity.getHeaders();
                MediaType requestContentType = requestHeaders.getContentType();
                for (HttpMessageConverter<?> messageConverter : getMessageConverters()) {
                    if (messageConverter instanceof GenericHttpMessageConverter) {
                        GenericHttpMessageConverter<Object> genericConverter =
                            (GenericHttpMessageConverter<Object>) messageConverter;
                        if (genericConverter.canWrite(requestBodyType, requestBodyClass, requestContentType)) {
                            if (!requestHeaders.isEmpty()) {
                                requestHeaders.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
                            }
                            logBody(requestBody, requestContentType, genericConverter);
                            genericConverter.write(requestBody, requestBodyType, requestContentType, httpRequest);
                            return;
                        }
                    } else if (messageConverter.canWrite(requestBodyClass, requestContentType)) {
                        if (!requestHeaders.isEmpty()) {
                            requestHeaders.forEach((key, values) -> httpHeaders.put(key, new LinkedList<>(values)));
                        }
                        logBody(requestBody, requestContentType, messageConverter);
                        ((HttpMessageConverter<Object>) messageConverter).write(
                            requestBody, requestContentType, httpRequest);
                        return;
                    }
                }
                String message = "No HttpMessageConverter for " + requestBodyClass.getName();
                if (requestContentType != null) {
                    message += " and content type \"" + requestContentType + "\"";
                }
                throw new RestClientException(message);
            }
        }

        private void logBody(Object body, @Nullable MediaType mediaType, HttpMessageConverter<?> converter) {
            if (logger.isDebugEnabled()) {
                if (mediaType != null) {
                    logger.debug("Writing [" + body + "] as \"" + mediaType + "\"");
                } else {
                    logger.debug("Writing [" + body + "] with " + converter.getClass().getName());
                }
            }
        }
    }

    /**
     * Request callback implementation that prepares the request's accept headers.
     */
    private class AcceptHeaderRequestCallback implements RequestCallback {

        @Nullable
        private final Type responseType;

        public AcceptHeaderRequestCallback(@Nullable Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public void doWithRequest(ClientHttpRequest request) throws IOException {
            if (this.responseType != null) {
                List<MediaType> allSupportedMediaTypes = getMessageConverters().stream()
                    .filter(converter -> canReadResponse(this.responseType, converter))
                    .flatMap(this::getSupportedMediaTypes)
                    .distinct()
                    .sorted(MediaType.SPECIFICITY_COMPARATOR)
                    .collect(Collectors.toList());
                if (logger.isDebugEnabled()) {
                    logger.debug("Accept=" + allSupportedMediaTypes);
                }
                request.getHeaders().setAccept(allSupportedMediaTypes);
            }
        }

        private boolean canReadResponse(Type responseType, HttpMessageConverter<?> converter) {
            Class<?> responseClass = (responseType instanceof Class ? (Class<?>) responseType : null);
            if (responseClass != null) {
                return converter.canRead(responseClass, null);
            } else if (converter instanceof GenericHttpMessageConverter) {
                GenericHttpMessageConverter<?> genericConverter = (GenericHttpMessageConverter<?>) converter;
                return genericConverter.canRead(responseType, null, null);
            }
            return false;
        }

        private Stream<MediaType> getSupportedMediaTypes(HttpMessageConverter<?> messageConverter) {
            return messageConverter.getSupportedMediaTypes()
                .stream()
                .map(mediaType -> {
                    if (mediaType.getCharset() != null) {
                        return new MediaType(mediaType.getType(), mediaType.getSubtype());
                    }
                    return mediaType;
                });
        }
    }

}
