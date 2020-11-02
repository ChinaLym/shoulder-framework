package org.shoulder.crypto.negotiation.support.server;

import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.negotiation.cache.KeyNegotiationCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cipher.DefaultTransportCipher;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.exception.NegotiationErrorCodeEnum;
import org.shoulder.crypto.negotiation.support.Sensitive;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 服务端敏感api接口拦截器 - 拦截带 @Sensitive 的接口，若请求头中不携带约定参数，则拒绝访问。
 * 只拦截握手完毕后的加密接口，即只拦截header中带 xSessionId 和 xDk 的请求。
 * order: 安全过滤/拦截器常设置为最早生效，如监控、日志拦截器之后，其他拦截器之前，具体顺序由具体场景决定
 *
 * @author lym
 * @see SensitiveRequestDecryptAdvance 实际解密在这里完成
 */
public class SensitiveRequestDecryptHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(SensitiveRequestDecryptHandlerInterceptor.class);

    private KeyNegotiationCache keyNegotiationCache;

    private TransportCryptoUtil transportCryptoUtil;

    public SensitiveRequestDecryptHandlerInterceptor(KeyNegotiationCache keyNegotiationCache, TransportCryptoUtil transportCryptoUtil) {
        this.keyNegotiationCache = keyNegotiationCache;
        this.transportCryptoUtil = transportCryptoUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //if (this.skipInterceptor(request, handler)) {
        //    return true;
        //} else {
        //}
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hMethod = (HandlerMethod) handler;
        Sensitive sensitiveAnnotation = hMethod.getMethod().getAnnotation(Sensitive.class);
        if (sensitiveAnnotation == null) {
            // 只拦截带 @Sensitive 的接口
            return true;
        }

        String xSessionId = request.getHeader(KeyExchangeConstants.SECURITY_SESSION_ID);
        String xDk = request.getHeader(KeyExchangeConstants.SECURITY_DATA_KEY);
        String token = request.getHeader(KeyExchangeConstants.TOKEN);
        if (log.isDebugEnabled()) {
            // 记录请求中这几个重要地参数，便于排查问题
            log.debug("xSessionId: {}, xDk: {}, token: {}.", xSessionId, xDk, token);
        }
        if (StringUtils.isEmpty(xSessionId) || StringUtils.isEmpty(xDk) || StringUtils.isEmpty(token)) {
            // xSessionId 没有说明不是一个 ecdh 请求；没有 xDk 仅出现在密钥协商阶段；没有 token不能保证安全
            log.debug("reject for invalid security headers.");
            return false;
        }

        KeyExchangeResult cacheKeyExchangeResult = keyNegotiationCache.getAsServer(xSessionId);
        if (cacheKeyExchangeResult == null) {
            // 返回重新握手错误码
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            RestResult r = RestResult.error(NegotiationErrorCodeEnum.NEGOTIATION_INVALID);
            response.getWriter().write(JsonUtils.toJson(r));
            return false;
        }
        KeyNegotiationCache.SERVER_LOCAL_CACHE.set(cacheKeyExchangeResult);

        // 校验token是否正确
        if (!transportCryptoUtil.verifyToken(xSessionId, xDk, token)) {
            log.debug("Token({}) invalid!", token);
            return false;
        }

        // 解密本次会话的数据密钥
        byte[] requestDk = TransportCryptoUtil.decryptDk(cacheKeyExchangeResult, xDk);
        // 缓存请求解密处理器
        DefaultTransportCipher requestDecryptCipher = DefaultTransportCipher.buildDecryptCipher(cacheKeyExchangeResult, requestDk);
        TransportCipherHolder.setRequestCipher(requestDecryptCipher);

        return true;
    }

}
