package org.shoulder.crypto.negotiation.support.service.impl;

import cn.hutool.core.lang.Assert;
import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.dto.KeyExchangeRequest;
import org.shoulder.crypto.negotiation.support.dto.KeyExchangeResponse;
import org.shoulder.crypto.negotiation.support.dto.TransportNegotiationInfo;
import org.shoulder.crypto.negotiation.support.service.TransportNegotiationService;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.shoulder.http.AppIdExtractor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.*;

/**
 * 安全会话，密钥协商默认逻辑
 *
 * @author lym
 */
public class TransportNegotiationServiceImpl implements TransportNegotiationService {

    private final Logger log = LoggerFactory.getLogger(getClass());

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
        String appId = appIdExtractor.extract(uri);
        try {
            // 1. 先尝试走缓存
            KeyExchangeResult cacheResult = keyNegotiationCache.getAsClient(appId);
            if (cacheResult != null) {
                return cacheResult;
            }

            // 2. 缓存不存在，发协商请求
            String negotiationUrl = getNegotiationUrl(appId);
            // 组装密钥交换请求地址
            String dslAimUrl = uri.toString().replace(uri.getPath(), negotiationUrl);
            log.debug("negotiate with {}, url is {}", appId, dslAimUrl);

            ParameterizedTypeReference<RestResult<KeyExchangeResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<RestResult<KeyExchangeResponse>> httpResponse =
                restTemplate.exchange(dslAimUrl, HttpMethod.POST, createKeyNegotiationHttpEntity(), responseType);

            // 3. 校验密钥协商的结果
            KeyExchangeResponse keyExchangeResponse = validateAndFill(httpResponse);

            // 4. 交换密钥
            KeyExchangeResult result = transportCryptoUtil.negotiation(keyExchangeResponse);

            // 5. 放缓存
            keyNegotiationCache.putAsClient(appId, result);
            return result;
        } catch (RestClientException restClientEx) {
            // 接口都没调通，应该检查是否写错了
            throw new NegotiationException("Try negotiate FAIL with " + appId + ", please check other service's healthy", restClientEx);
        } catch (AsymmetricCryptoException e) {
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
     * 校验密钥协商响应是否合法，token 部分
     * 并将 xSessionId、token 从请求头中放到返回值中
     *
     * @param httpResponse 密钥协商响应
     * @return 合法的响应
     */
    private KeyExchangeResponse validateAndFill(ResponseEntity<RestResult<KeyExchangeResponse>> httpResponse) throws AsymmetricCryptoException, NegotiationException {
        RestResult<KeyExchangeResponse> response = httpResponse.getBody();
        if (HttpStatus.OK != httpResponse.getStatusCode() || response == null) {
            throw new NegotiationException("response error! response = " + JsonUtils.toJson(response));
        }
        KeyExchangeResponse keyExchangeResponse = response.getData();
        String token = httpResponse.getHeaders().getFirst(KeyExchangeConstants.TOKEN);
        String xSessionId = httpResponse.getHeaders().getFirst(KeyExchangeConstants.SECURITY_SESSION_ID);
        String publicKey = keyExchangeResponse.getPublicKey();
        String aes = keyExchangeResponse.getAes();

        Assert.notEmpty(publicKey, "response.publicKey can't be empty");
        Assert.notEmpty(aes, "response.aes can't be empty");

        keyExchangeResponse.setxSessionId(xSessionId);
        keyExchangeResponse.setToken(token);

        if (!transportCryptoUtil.verifyToken(keyExchangeResponse)) {
            throw new NegotiationException("token not validate!");
        }

        return keyExchangeResponse;
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
        KeyExchangeResult keyExchangeResult;
        try {
            // 验证协商的参数与签名
            validateAndFill(keyExchangeRequest);
            if (!keyExchangeRequest.isRefresh()) {
                //不强制刷新 尝试走缓存
                keyExchangeResult = keyNegotiationCache.getAsServer(keyExchangeRequest.getxSessionId());
                if (keyExchangeResult != null) {
                    return transportCryptoUtil.createResponse(keyExchangeResult);
                }
            }

            // 准备协商
            KeyExchangeResponse response = transportCryptoUtil.prepareNegotiation(keyExchangeRequest);
            KeyExchangeResponse negotiationParam = response.clone();
            negotiationParam.setPublicKey(keyExchangeRequest.getPublicKey());
            // 协商密钥，生成 shareKey、根据 shareKey 生成 localKey，localIv
            KeyExchangeResult result = transportCryptoUtil.negotiation(negotiationParam);
            long expireTime = result.getExpireTime();
            // 放缓存 (作为响应方 协商缓存失效时间加1分钟，以尽量保证比对方提前过期，避免临界时大量请求失败)
            result.setExpireTime(expireTime + 60 * 1000);
            keyNegotiationCache.putAsServer(response.getxSessionId(), result);

            // processHeaders
            HttpServletResponse httpResponse = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            assert httpResponse != null;
            httpResponse.setHeader(KeyExchangeConstants.SECURITY_SESSION_ID, response.getxSessionId());
            httpResponse.setHeader(KeyExchangeConstants.TOKEN, response.getToken());

            return response;
        } catch (AsymmetricCryptoException e) {
            // todo 【规范】抛出带错误码的异常，方便客户端处理
            throw new NegotiationException("Receive request, negotiate Fail!", e);
        }
    }


    /**
     * 校验请求是否合法，token 部分
     * 并将 xSessionId、token 从请求头中放到返回值中
     */
    private KeyExchangeRequest validateAndFill(@Nonnull KeyExchangeRequest keyExchangeRequest) throws AsymmetricCryptoException, NegotiationException {

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String xSessionId = request.getHeader(KeyExchangeConstants.SECURITY_SESSION_ID);
        String token = request.getHeader(KeyExchangeConstants.TOKEN);

        keyExchangeRequest.setxSessionId(xSessionId);
        keyExchangeRequest.setToken(token);

        if (!transportCryptoUtil.verifyToken(keyExchangeRequest)) {
            // todo 【健壮性】token 不合法，一般仅发生于网络通信被劫持且篡改时，回应 400 拒绝握手请求，记录不合法日志
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
    @Nonnull
    private String getNegotiationUrl(String appId) {
        return this.negotiationUrls.computeIfAbsent(appId, serviceIndex -> {
            log.warn("Not config [{}]'s negotiationUrl, will use default: " +
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
    public boolean isNegotiationUrl(@Nonnull URI uri) {
        String appId = appIdExtractor.extract(uri);
        return uri.getPath().equalsIgnoreCase(negotiationUrls.get(appId));
    }

}
