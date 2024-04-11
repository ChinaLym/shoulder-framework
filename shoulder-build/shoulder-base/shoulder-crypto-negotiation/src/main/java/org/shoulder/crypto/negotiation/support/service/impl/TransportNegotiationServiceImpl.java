package org.shoulder.crypto.negotiation.support.service.impl;

import cn.hutool.core.lang.Assert;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.negotiation.cache.NegotiationResultCache;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.dto.NegotiationRequest;
import org.shoulder.crypto.negotiation.support.dto.NegotiationResponse;
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

import java.net.URI;
import java.util.*;

/**
 * 安全会话，密钥协商默认逻辑
 *
 * @author lym
 */
public class TransportNegotiationServiceImpl implements TransportNegotiationService {

    private final Logger log = ShoulderLoggers.SHOULDER_CRYPTO;

    private final TransportCryptoUtil transportCryptoUtil;

    /**
     * 带寻址能力的 RestTemplate
     */
    private final RestTemplate restTemplate;

    private final NegotiationResultCache negotiationResultCache;

    private final AppIdExtractor appIdExtractor;

    private Map<String, String> negotiationUrls = new HashMap<>();

    public TransportNegotiationServiceImpl(TransportCryptoUtil transportCryptoUtil, RestTemplate restTemplate,
                                           NegotiationResultCache negotiationResultCache, AppIdExtractor appIdExtractor) {
        this.transportCryptoUtil = transportCryptoUtil;
        this.restTemplate = restTemplate;
        this.negotiationResultCache = negotiationResultCache;
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
    public NegotiationResult requestForNegotiate(URI uri) throws NegotiationException {
        String appId = appIdExtractor.extract(uri);
        try {
            // 1. 先尝试走缓存
            NegotiationResult cacheResult = negotiationResultCache.getAsClient(appId);
            if (cacheResult != null) {
                return cacheResult;
            }

            // 2. 缓存不存在，发协商请求
            String negotiationUrl = getNegotiationUrl(appId);
            // 组装密钥交换请求地址
            String dslAimUrl = uri.toString().replace(uri.getPath(), negotiationUrl);
            log.debug("negotiate with {}, url is {}", appId, dslAimUrl);

            ParameterizedTypeReference<BaseResult<NegotiationResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<BaseResult<NegotiationResponse>> httpResponse =
                    restTemplate.exchange(dslAimUrl, HttpMethod.POST, createKeyNegotiationHttpEntity(), responseType);

            // 3. 提取并校验密钥协商响应
            NegotiationResponse negotiationResponse = validateAndFill(httpResponse);

            // 4. 交换密钥
            NegotiationResult result = transportCryptoUtil.negotiation(negotiationResponse);

            // 5. 放缓存
            negotiationResultCache.putAsClient(appId, result);
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
    private HttpEntity<NegotiationRequest> createKeyNegotiationHttpEntity() throws AsymmetricCryptoException {
        // 1. 创建 body
        NegotiationRequest requestParam = transportCryptoUtil.createRequest();

        // 2. 处理请求头，如 token 相关
        String xSessionId = requestParam.getxSessionId();
        String token = requestParam.getToken();

        HttpHeaders httpHeaders = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.setAccept(mediaTypes);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.add(NegotiationConstants.SECURITY_SESSION_ID, xSessionId);
        httpHeaders.add(NegotiationConstants.TOKEN, token);

        return new HttpEntity<>(requestParam, httpHeaders);
    }

    /**
     * 校验密钥协商响应是否合法，token 部分
     * 并将 xSessionId、token 从请求头中放到返回值中
     *
     * @param httpResponse 密钥协商响应
     * @return 合法的响应
     */
    private NegotiationResponse validateAndFill(ResponseEntity<BaseResult<NegotiationResponse>> httpResponse) throws AsymmetricCryptoException, NegotiationException {
        BaseResult<NegotiationResponse> response = httpResponse.getBody();
        if (HttpStatus.OK != httpResponse.getStatusCode() || response == null) {
            throw new NegotiationException("response error! response = " + JsonUtils.toJson(response));
        }
        NegotiationResponse negotiationResponse = response.getData();
        String token = httpResponse.getHeaders().getFirst(NegotiationConstants.TOKEN);
        String xSessionId = httpResponse.getHeaders().getFirst(NegotiationConstants.SECURITY_SESSION_ID);
        String publicKey = negotiationResponse.getPublicKey();
        String encryptionScheme = negotiationResponse.getEncryptionScheme();

        Assert.notEmpty(publicKey, "response.publicKey can't be empty");
        Assert.notEmpty(encryptionScheme, "response.encryptionScheme can't be empty");
        // 校验报文加密算法自身支持，对方没有乱返回

        negotiationResponse.setxSessionId(xSessionId);
        negotiationResponse.setToken(token);

        if (!transportCryptoUtil.verifyToken(negotiationResponse)) {
            throw new NegotiationException("token not validate!");
        }

        return negotiationResponse;
    }


    // ===============================  处理请求  ==============================

    /**
     * 用于服务端处理调用方发来的密钥协商请求
     *
     * @param negotiationRequest 请求参数
     * @return 协商响应
     * @throws NegotiationException 协商异常
     */
    @Override
    public NegotiationResponse handleNegotiate(NegotiationRequest negotiationRequest) throws NegotiationException {
        NegotiationResult negotiationResult;
        try {
            // 验证协商的参数与签名
            validateAndFill(negotiationRequest);
            if (!negotiationRequest.isRefresh()) {
                //不强制刷新 尝试走缓存
                negotiationResult = negotiationResultCache.getAsServer(negotiationRequest.getxSessionId());
                if (negotiationResult != null) {
                    return transportCryptoUtil.createResponse(negotiationResult);
                }
            }

            // 准备协商
            NegotiationResponse response = transportCryptoUtil.prepareNegotiation(negotiationRequest);
            NegotiationResponse negotiationParam = response.clone();
            negotiationParam.setPublicKey(negotiationRequest.getPublicKey());
            // 协商密钥，生成 shareKey、根据 shareKey 生成 localKey，localIv
            NegotiationResult result = transportCryptoUtil.negotiation(negotiationParam);
            long expireTime = result.getExpireTime();
            // 放缓存 (作为响应方 协商缓存失效时间加1分钟，以尽量保证比对方提前过期，避免临界时大量请求失败)
            result.setExpireTime(expireTime + 60 * 1000);
            negotiationResultCache.putAsServer(response.getxSessionId(), result);

            // processHeaders
            HttpServletResponse httpResponse = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            assert httpResponse != null;
            httpResponse.setHeader(NegotiationConstants.SECURITY_SESSION_ID, response.getxSessionId());
            httpResponse.setHeader(NegotiationConstants.TOKEN, response.getToken());

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
    private NegotiationRequest validateAndFill(@Nonnull NegotiationRequest negotiationRequest) throws AsymmetricCryptoException, NegotiationException {

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String xSessionId = request.getHeader(NegotiationConstants.SECURITY_SESSION_ID);
        String token = request.getHeader(NegotiationConstants.TOKEN);

        negotiationRequest.setxSessionId(xSessionId);
        negotiationRequest.setToken(token);

        if (!transportCryptoUtil.verifyToken(negotiationRequest)) {
            // todo 【健壮性】token 不合法，一般仅发生于网络通信被劫持且篡改时，回应 400 拒绝握手请求，记录不合法日志
            throw new NegotiationException("token not validate!");
        }
        return negotiationRequest;
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
                NegotiationConstants.DEFAULT_NEGOTIATION_URL, appId);
            return NegotiationConstants.DEFAULT_NEGOTIATION_URL;
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
