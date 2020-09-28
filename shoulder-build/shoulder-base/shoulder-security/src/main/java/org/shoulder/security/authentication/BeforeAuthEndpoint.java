package org.shoulder.security.authentication;

import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.StringUtils;
import org.shoulder.security.SecurityConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 默认的 待认证请求处理器。当未认证（未登录）时访问需要认证才能访问的资源时，会统一跳转至这里
 *
 * @author lym
 * @see LoginUrlAuthenticationEntryPoint spring security 默认
 */
@RestController
public class BeforeAuthEndpoint {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 登录页面，为 null 则不会触发跳转
     */
    private final String signInPage;

    /**
     * spring security 会将待认证的请求放到这里
     */
    private final RequestCache requestCache;

    /**
     * 重定向策略
     */
    private final RedirectStrategy redirectStrategy;

    public BeforeAuthEndpoint(@Nullable String signInPage) {
        this.signInPage = signInPage;
        if (signInPage != null) {
            if (SecurityConst.URL_REQUIRE_AUTHENTICATION.equalsIgnoreCase(signInPage)) {
                // 自身不能作为登录页面（重定向目标地址），否则会导致无限重定向
                throw new IllegalArgumentException("invalid loginPage!");
            }
            requestCache = new HttpSessionRequestCache();
            redirectStrategy = new DefaultRedirectStrategy();
        } else {
            // 为 null 则不会触发跳转，直接返回 json
            requestCache = null;
            redirectStrategy = null;
        }
    }

    /**
     * 当需要身份认证时，跳转到这里
     * 一般返回给用户一个登录页面
     * spring-security 的实现存在无限跳转登陆页面的bug，这里解决掉
     */
    @RequestMapping(SecurityConst.URL_REQUIRE_AUTHENTICATION)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public RestResult requireAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        if (signInPage == null) {
            return new RestResult(CommonErrorCodeEnum.AUTH_401_NEED_AUTH);
        }

        // 是否有认证错误
        String failReason = (String) request.getAttribute(SecurityConst.AUTH_FAIL_PARAM_NAME);
        boolean withoutError = StringUtils.isBlank(failReason) || "null".equalsIgnoreCase(failReason);

        if (returnJson(request, response)) {
            log.trace("json type");
            // json 响应
            return withoutError ? new RestResult(CommonErrorCodeEnum.AUTH_401_NEED_AUTH) :
                new RestResult<>(CommonErrorCodeEnum.AUTH_401_NEED_AUTH).setData(failReason);
        }
        log.trace("redirect to signInPage({})", signInPage);

        // 跳转
        String redirectSignInUrl = withoutError ? signInPage :
            signInPage + "?" + SecurityConst.AUTH_FAIL_PARAM_NAME + "=" + failReason;
        redirectStrategy.sendRedirect(request, response, redirectSignInUrl);

        return new RestResult(CommonErrorCodeEnum.AUTH_401_NEED_AUTH);
    }


    /**
     * 响应格式为 json 还是 跳转
     *
     * @return 可以接受 json 的响应，则返回 json 否则跳转到指定的登录页面
     */
    protected boolean returnJson(HttpServletRequest request, HttpServletResponse response) {
        // 获取引发跳转的请求
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        // 可以接受 html 的响应，跳转到指定的登录认证页面，默认策略，如果支持 json，则返回 json
        //
        if (savedRequest != null) {
            List<String> headerAccepts = savedRequest.getHeaderValues(HttpHeaders.ACCEPT);
            for (String headerAccept : headerAccepts) {
                if (StringUtils.containsAny(headerAccept, "*/*", "json")) {
                    return true;
                }
            }
        } else {
            return StringUtils.containsAny(request.getHeader(HttpHeaders.ACCEPT), "*/*", "json");
        }
        return false;
    }

}
