package org.shoulder.crypto.negotiation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.dto.KeyExchangeRequest;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResponse;
import org.shoulder.crypto.negotiation.dto.TransportNegotiationInfo;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.shoulder.http.AppIdExtractor;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.*;

/**
 * 安全会话，密钥协商
 *
 * @author lym
 */
@Slf4j
public class TransportNegotiationServiceImpl implements TransportNegotiationService {

    private final TransportCryptoUtil transportCryptoUtil;

    /**
     * 带寻址能力的 RestTemplate
     */
    private final RestTemplate restTemplate;

    private final KeyNegotiationCache keyNegotiationCache;

    private final AppIdExtractor appIdExtractor;

    private Map<String, String> negotiationUrls = new HashMap<>();

    public TransportNegotiationServiceImpl(TransportCryptoUtil transportCryptoUtil, RestTemplate restTemplate,
                                           KeyNegotiationCache keyNegotiationCache, AppIdExtractor appIdExtractor) {
        this.transportCryptoUtil = transportCryptoUtil;
        this.restTemplate = restTemplate;
        this.keyNegotiationCache = keyNegotiationCache;
        this.appIdExtractor = appIdExtractor;
    }

    // ===============================  请求协商密钥  ==============================


    /**
     * 发起密钥协商请求
     *
     * @param uri 目标接口
     * @return 密钥协商结果
     * @throws NegotiationException 协商失败
     */
    @Override
    public KeyExchangeResult requestForNegotiate(URI uri) throws NegotiationException {
        return requestForNegotiate(appIdExtractor.extract(uri));
    }

    /**
     * 发起密钥协商请求
     *
     * @param appId 目标应用标识
     * @return 密钥协商结果
     * @throws NegotiationException 协商失败
     */
    @Override
    public KeyExchangeResult requestForNegotiate(String appId) throws NegotiationException {
        try {
            // 1. 先尝试走缓存
            KeyExchangeResult cacheResult = keyNegotiationCache.getAsClient(appId);
            if (cacheResult != null) {
                return cacheResult;
            }

            // 2. 缓存不存在，发协商请求
            String negotiationUrl = getNegotiationUrl(appId);
            // 通过应用标识组装 http 地址
            String dslAimUrl = "http://" + appId + negotiationUrl;
            log.info("negotiate with {}, url is {}", appId, dslAimUrl);
            ResponseEntity<KeyExchangeResponse> httpResponse =
                restTemplate.postForEntity(negotiationUrl, createKeyNegotiationHttpEntity(), KeyExchangeResponse.class);

            // 3. 校验密钥协商的结果
            KeyExchangeResponse keyExchangeResponse = validateAndFill(httpResponse);

            // 4. 交换密钥
            KeyExchangeResult result = transportCryptoUtil.negotiation(keyExchangeResponse);

            // 5. 放缓存
            keyNegotiationCache.putAsClient(appId, result);
            return result;
        } catch (Exception e) {
            throw new NegotiationException("Negotiate FAIL with " + appId, e);
        }
    }

    /**
     * 创建协商请求体
     */
    private HttpEntity<KeyExchangeRequest> createKeyNegotiationHttpEntity() throws AsymmetricCryptoException {
        // 1. 创建 body
        KeyExchangeRequest requestParam = transportCryptoUtil.createRequest();

        // 2. 处理请求头，如 token 相关
        String xSessionId = requestParam.getxSessionId();
        String token = requestParam.getToken();

        HttpHeaders httpHeaders = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.setAccept(mediaTypes);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.add(KeyExchangeConstants.SECURITY_SESSION_ID, xSessionId);
        httpHeaders.add(KeyExchangeConstants.TOKEN, token);

        return new HttpEntity<>(requestParam, httpHeaders);
    }

    /**
     * 校验响应是否合法，token 部分
     * 并将 xSessionId、token 从请求头中放到返回值中
     *
     * @param httpResponse 握手响应
     * @return 合法的响应
     */
    private KeyExchangeResponse validateAndFill(ResponseEntity<KeyExchangeResponse> httpResponse) throws AsymmetricCryptoException, NegotiationException {
        KeyExchangeResponse response = httpResponse.getBody();
        if (HttpStatus.OK != httpResponse.getStatusCode() || response == null) {
            throw new NegotiationException("response error! response = " + (response == null ? "null" : response.toString()));
        }
        String token = httpResponse.getHeaders().getFirst(KeyExchangeConstants.TOKEN);
        String xSessionId = httpResponse.getHeaders().getFirst(KeyExchangeConstants.SECURITY_SESSION_ID);

        response.setxSessionId(xSessionId);
        response.setToken(token);

        if (!transportCryptoUtil.verifyResponseToken(response)) {
            throw new NegotiationException("token not validate!");
        }

        return response;
    }


    // ===============================  处理请求  ==============================

    /**
     * 用于服务端处理调用方发来的密钥协商请求
     *
     * @param keyExchangeRequest 请求参数
     * @return 协商响应
     * @throws NegotiationException 协商异常
     */
    @Override
    public KeyExchangeResponse handleNegotiate(KeyExchangeRequest keyExchangeRequest) throws NegotiationException {
        KeyExchangeResult keyExchangeResult = null;
        // 校验
        try {
            validateAndFill(keyExchangeRequest);
            if (!keyExchangeRequest.isRefresh()) {
                //不强制刷新 尝试走缓存
                keyExchangeResult = keyNegotiationCache.getAsServer(keyExchangeRequest.getxSessionId());
                if (keyExchangeResult != null) {
                    return generateResponse(keyExchangeResult);
                }
            }

            // 交换密钥
            KeyExchangeResponse response = transportCryptoUtil.negotiation(keyExchangeRequest);

            // 放缓存
            KeyExchangeResult result = transportCryptoUtil.negotiation(response);
            keyNegotiationCache.putAsServer(response.getxSessionId(), result);

            // processHeaders
            HttpServletResponse httpResponse = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            assert httpResponse != null;
            httpResponse.setHeader(KeyExchangeConstants.SECURITY_SESSION_ID, response.getxSessionId());
            httpResponse.setHeader(KeyExchangeConstants.TOKEN, response.getToken());

            return response;
        } catch (Exception e) {
            throw new NegotiationException("Receive request, negotiate Fail!", e);
        }
    }

    /**
     * 根据缓存内容生成握手响应
     */
    private KeyExchangeResponse generateResponse(KeyExchangeResult keyExchangeResult) throws AsymmetricCryptoException {
        KeyExchangeResponse response = new KeyExchangeResponse();

        byte[] publicKey = keyExchangeResult.getPublicKey();

        response.setPublicKey(ByteSpecification.encodeToString(publicKey));
        response.setExpireTime((int) (keyExchangeResult.getExpireTime() - System.currentTimeMillis()));
        response.setKeyLength(keyExchangeResult.getKeyLength());
        response.setAes("256");

        response.setxSessionId(keyExchangeResult.getxSessionId());
        String token = transportCryptoUtil.generateResponseToken(response);
        response.setToken(token);
        return response;
    }

    /**
     * 校验请求是否合法，token 部分
     * 并将 xSessionId、token 从请求头中放到返回值中
     */
    private KeyExchangeRequest validateAndFill(@NonNull KeyExchangeRequest keyExchangeRequest) throws AsymmetricCryptoException, NegotiationException {

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String xSessionId = request.getHeader(KeyExchangeConstants.SECURITY_SESSION_ID);
        String token = request.getHeader(KeyExchangeConstants.TOKEN);

        keyExchangeRequest.setxSessionId(xSessionId);
        keyExchangeRequest.setToken(token);

        if (!transportCryptoUtil.verifyRequestToken(keyExchangeRequest)) {
            throw new NegotiationException("token not validate!");
        }
        return keyExchangeRequest;
    }

    /**
     * 获取与目标服务的密钥协商地址，如果不存在则返回默认地址
     *
     * @param appId 应用标识
     * @return negotiationUrl
     */
    @NonNull
    private String getNegotiationUrl(String appId) {
        return this.negotiationUrls.computeIfAbsent(appId, serviceIndex -> {
            log.warn("Not config [{}]'s negotiationUrl, will use default" +
                KeyExchangeConstants.DEFAULT_NEGOTIATION_URL, appId);
            return KeyExchangeConstants.DEFAULT_NEGOTIATION_URL;
        });
    }

    /**
     * 服务名，服务的协商地址
     *
     * @param negotiationInfo 应用标识和对应的密钥协商地址
     */
    public void addNegotiationUrl(TransportNegotiationInfo negotiationInfo) {
        this.negotiationUrls.put(negotiationInfo.getAppId(), negotiationInfo.getNegotiationUrl());
    }

    @Override
    public boolean isNegotiationUrl(@NonNull URI uri) {
        String appId = appIdExtractor.extract(uri);
        return uri.getPath().equalsIgnoreCase(negotiationUrls.get(appId));
    }

}
