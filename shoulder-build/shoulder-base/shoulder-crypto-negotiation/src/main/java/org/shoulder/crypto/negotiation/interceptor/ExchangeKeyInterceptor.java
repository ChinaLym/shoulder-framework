package org.shoulder.crypto.negotiation.interceptor;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;
import org.shoulder.crypto.negotiation.cache.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全传输拦截器
 * 只拦截握手完毕后的加密接口，即只拦截header中带 xSessionId 和 xDk 的请求。
 *
 * @author lym
 */
@Component
public class ExchangeKeyInterceptor extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(ExchangeKeyInterceptor.class);

    private KeyNegotiationCache keyNegotiationCache;

    private TransportCryptoUtil transportCryptoUtil;

    public ExchangeKeyInterceptor(KeyNegotiationCache keyNegotiationCache, TransportCryptoUtil transportCryptoUtil) {
        this.keyNegotiationCache = keyNegotiationCache;
        this.transportCryptoUtil = transportCryptoUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(KeyExchangeConstants.TOKEN);
        String xSessionId = request.getHeader(KeyExchangeConstants.SECURITY_SESSION_ID);
        String xDk = request.getHeader(KeyExchangeConstants.SECURITY_DATA_KEY);
        if (StringUtils.isEmpty(xSessionId) || StringUtils.isEmpty(xDk) || StringUtils.isEmpty(token)) {
            return true;
        }

        // 一、处理请求：解密发送方的会话密钥
        KeyExchangeResult cacheKeyExchangeResult = keyNegotiationCache.getAsServer(xSessionId);

        if(cacheKeyExchangeResult == null){
            // 返回重新握手错误码
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            BaseResponse r = BaseResponse.error(CommonErrorCodeEnum.SECURITY_SESSION_INVALID);
            response.getWriter().write(JsonUtils.toJson(r));
            return false;
        }

        transportCryptoUtil.verifyToken(xSessionId, xDk, token);

        log.debug("security request. xDk is " + xDk);
        // 解密数据密钥
        byte[] requestDk = TransportCryptoUtil.decryptDk(cacheKeyExchangeResult, xDk);
        // 缓存请求解密处理器
        TransportCipher requestDecryptor = TransportCipher.decryptor(cacheKeyExchangeResult, requestDk);
        TransportCipherHolder.setRequestDecryptor(requestDecryptor);

        // 二、预生成返回值加密的数据密钥，以便于加密要返回的敏感数据信息（请求和响应中使用的数据密钥不同）
        byte[] responseDk = TransportCryptoUtil.generateDataKey(cacheKeyExchangeResult.getKeyLength());
        // 缓存响应加密处理器
        TransportCipher responseEncrypt = TransportCipher.encryptor(cacheKeyExchangeResult, responseDk);
        TransportCipherHolder.setResponseEncryptor(responseEncrypt);
        String responseX_Dk = TransportCryptoUtil.encryptDk(cacheKeyExchangeResult, responseDk);
        log.info("security response. xDk is " + responseX_Dk);
        response.setHeader("Token", transportCryptoUtil.generateToken(xSessionId, responseX_Dk));
        response.setHeader("xSessionId", cacheKeyExchangeResult.getxSessionId());
        response.setHeader("xDk", responseX_Dk);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        return true;
    }

}
