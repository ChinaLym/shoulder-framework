package org.shoulder.crypto.negotiation.support.server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.negotiation.cache.NegotiationResultCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cipher.DefaultTransportCipher;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.exception.NegotiationErrorCodeEnum;
import org.shoulder.crypto.negotiation.support.Sensitive;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * 服务端敏感api接口拦截器 - 拦截带 @Sensitive 的接口，若请求头中不携带约定参数，则拒绝访问。
 * 只拦截握手完毕后的加密接口，即只拦截header中带 xSessionId 和 xDk 的请求。
 * order: 安全过滤/拦截器常设置为最早生效，如监控、日志拦截器之后，其他拦截器之前，具体顺序由具体场景决定
 *
 * @author lym
 * @see SensitiveRequestDecryptAdvance 实际解密在这里完成
 */
public class SensitiveRequestDecryptHandlerInterceptor implements AsyncHandlerInterceptor {

    private static final Logger log = ShoulderLoggers.SHOULDER_WEB;

    private NegotiationResultCache negotiationResultCache;

    private TransportCryptoUtil transportCryptoUtil;

    public SensitiveRequestDecryptHandlerInterceptor(NegotiationResultCache negotiationResultCache, TransportCryptoUtil transportCryptoUtil) {
        this.negotiationResultCache = negotiationResultCache;
        this.transportCryptoUtil = transportCryptoUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //if (this.skipInterceptor(request, handler)) {
        //    return true;
        //} else {
        //}
        if (!(handler instanceof HandlerMethod hMethod)) {
            return true;
        }
        Sensitive sensitiveAnnotation = hMethod.getMethod().getAnnotation(Sensitive.class);
        if (sensitiveAnnotation == null) {
            // 只拦截带 @Sensitive 的接口
            return true;
        }

        String xSessionId = request.getHeader(NegotiationConstants.SECURITY_SESSION_ID);
        String xDk = request.getHeader(NegotiationConstants.SECURITY_DATA_KEY);
        String token = request.getHeader(NegotiationConstants.TOKEN);
        if (log.isDebugEnabled()) {
            // 记录请求中这几个重要地参数，便于排查问题
            log.debug("xSessionId: {}, xDk: {}, token: {}.", xSessionId, xDk, token);
        }
        if (StringUtils.isEmpty(xSessionId) || StringUtils.isEmpty(xDk) || StringUtils.isEmpty(token)) {
            // xSessionId 没有说明不是一个 ecdh 请求；没有 xDk 仅出现在密钥协商阶段；没有 token 不能保证安全
            log.debug("reject for invalid security headers.");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            BaseResult<Void> r = BaseResult.error(NegotiationErrorCodeEnum.MISSING_REQUIRED_PARAM);
            response.getWriter().write(JsonUtils.toJson(r));
            return false;
        }

        NegotiationResult cacheNegotiationResult = negotiationResultCache.getAsServer(xSessionId);
        if (cacheNegotiationResult == null) {
            // 恶意调用 / 本服务缓存丢失（如重启导致）
            log.debug("cache missing, xSessionId:{}", xSessionId);
            // 返回重新握手错误码
            response.setHeader(NegotiationConstants.NEGOTIATION_INVALID_TAG, NegotiationErrorCodeEnum.NEGOTIATION_INVALID.getCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            BaseResult<Void> r = BaseResult.error(NegotiationErrorCodeEnum.NEGOTIATION_INVALID);
            response.getWriter().write(JsonUtils.toJson(r));
            return false;
        }
        NegotiationResultCache.SERVER_LOCAL_CACHE.set(cacheNegotiationResult);

        // 校验token是否正确
        if (!transportCryptoUtil.verifyToken(xSessionId, xDk, token, cacheNegotiationResult.getOtherPublicKey())) {
            log.debug("Token({}) invalid! xSessionId={}", token, xSessionId);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            BaseResult<Void> r = BaseResult.error(NegotiationErrorCodeEnum.TOKEN_INVALID);
            response.getWriter().write(JsonUtils.toJson(r));
            return false;
        }

        // 解密本次会话的数据密钥
        byte[] requestDk = TransportCryptoUtil.decryptDk(cacheNegotiationResult, xDk);
        // 缓存请求解密处理器
        DefaultTransportCipher requestDecryptCipher = DefaultTransportCipher.buildDecryptCipher(cacheNegotiationResult, requestDk);
        TransportCipherHolder.setRequestCipher(requestDecryptCipher);

        return true;
    }

}
