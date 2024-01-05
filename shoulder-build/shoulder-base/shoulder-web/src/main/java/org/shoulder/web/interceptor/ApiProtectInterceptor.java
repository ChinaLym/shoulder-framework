package org.shoulder.web.interceptor;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * api 用于保护 api 不被意外调用 —— 所有调用 api 接口的方法必须携带特定格式 token
 * 适用于基于 Session 的 web 应用，检验是否存在合法 Token
 * 基于JWT的服务不必装配该拦截器，校验 JWT 即可
 *
 * @author lym
 */
public class ApiProtectInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiProtectInterceptor.class);

    @Value("${shoulder.web.waf.token.failed-code:Token}")
    public String tokenNameInHeader;

    public static final String TOKEN_MISS = "No AuthToken Request request!";


    public static final String TOKEN_INVALID = "AuthToken Invalid!";

    /**
     * token 验证失败的错误码
     */
    @Value("${shoulder.web.waf.token.failed-code:-1}")
    protected Long failedCode;

    /**
     * token 过期时返回的错误码
     */
    protected Long expireCode;

    @Value("#{'${shoulder.web.waf.token.ignore-urls:}'.split(',')}")
    protected List<String> ignoreUrls = new ArrayList<>();

    protected String failedCodeString = null;

    protected String expireCodeString = null;


    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        // 判断是否需要跳过此逻辑
        if (!(handler instanceof HandlerMethod) || this.skipInterceptor(request, handler)) {
            return true;
        }
        try {
            checkToken(this.getToken(request), null);
        } catch (Exception e) {
            throw CommonErrorCodeEnum.AUTH_403_TOKEN_INVALID.toException(e);
        }
        return true;
    }

    /**
     * 检查 token 是否合法
     *
     * @param token      token
     * @param signOrigin 签名元数据
     * @return token 是否合法
     */
    private boolean checkToken(String token, String signOrigin) {
        return true;
    }

    protected boolean skipInterceptor(HttpServletRequest request, Object handler) {
        // 放行不需要拦截的URL和methodNoNeedAuthentication方法
        String realRequestUrl = this.getRealRequestURI(request);
        if (this.ignoreUrl(realRequestUrl)) {
            return true;
        }
        return false;
    }

    private boolean ignoreUrl(String realRequestUrl) {
        return false;
    }

    protected String getRealRequestURI(HttpServletRequest req) {
        int startIndex = req.getRequestURI().indexOf('/', 1);
        if (startIndex != -1) {
            return req.getRequestURI().substring(startIndex);
        } else {
            return req.getRequestURI();
        }
    }

    protected String getToken(HttpServletRequest request) {

        String token = request.getHeader(tokenNameInHeader);
        if (StringUtils.isEmpty(token)) {
            // outErrorMessage是抛异常，所以不需要return false
            throw new BaseRuntimeException(TOKEN_MISS);
        }
        return token;
    }
}
