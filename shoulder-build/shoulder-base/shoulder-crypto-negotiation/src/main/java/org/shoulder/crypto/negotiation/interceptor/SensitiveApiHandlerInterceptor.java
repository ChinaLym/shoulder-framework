package org.shoulder.crypto.negotiation.interceptor;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.exception.CommonErrorCodeEnum;
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
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 服务端敏感api接口拦截器
 * 只拦截握手完毕后的加密接口，即只拦截header中带 xSessionId 和 xDk 的请求。todo 只拦截带 Sensitive 的接口
 *
 * @author lym
 */
public class SensitiveApiHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(SensitiveApiHandlerInterceptor.class);

    private KeyNegotiationCache keyNegotiationCache;

    private TransportCryptoUtil transportCryptoUtil;

    public SensitiveApiHandlerInterceptor(KeyNegotiationCache keyNegotiationCache, TransportCryptoUtil transportCryptoUtil) {
        this.keyNegotiationCache = keyNegotiationCache;
        this.transportCryptoUtil = transportCryptoUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String xSessionId = request.getHeader(KeyExchangeConstants.SECURITY_SESSION_ID);
        String xDk = request.getHeader(KeyExchangeConstants.SECURITY_DATA_KEY);
        String token = request.getHeader(KeyExchangeConstants.TOKEN);
        // todo 记录请求中这几个重要地参数
        if (StringUtils.isEmpty(xSessionId) || StringUtils.isEmpty(xDk) || StringUtils.isEmpty(token)) {
            // xSessionId 没有说明不是一个 ecdh 请求，没有 xDk 说明还在密钥协商阶段
            return true;
        }

        // 一、处理请求：解密发送方的会话密钥
        KeyExchangeResult cacheKeyExchangeResult = keyNegotiationCache.getAsServer(xSessionId);

        if (cacheKeyExchangeResult == null) {
            // 返回重新握手错误码
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            BaseResponse r = BaseResponse.error(CommonErrorCodeEnum.SECURITY_SESSION_INVALID);
            response.getWriter().write(JsonUtils.toJson(r));
            return false;
        }

        // 校验token是否正确
        transportCryptoUtil.verifyToken(xSessionId, xDk, token);

        log.debug("security request. xDk is " + xDk);
        // 解密数据密钥
        byte[] requestDk = TransportCryptoUtil.decryptDk(cacheKeyExchangeResult, xDk);
        // 缓存请求解密处理器
        TransportCipher requestDecryptCipher = TransportCipher.buildDecryptCipher(cacheKeyExchangeResult, requestDk);
        TransportCipherHolder.setRequestDecryptCipher(requestDecryptCipher);

        return true;
    }

}
