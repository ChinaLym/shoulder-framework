package org.shoulder.security.authentication.browser;

import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.security.SecurityConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class BrowserAuthEndpoint {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String signInPage;

    /**
     * spring security 会将待认证的请求放到这里
     */
    private RequestCache requestCache = new HttpSessionRequestCache();

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public BrowserAuthEndpoint(String signInPage) {
        this.signInPage = signInPage;
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

        // 获取引发跳转的请求
        SavedRequest savedRequest = requestCache.getRequest(request, response);


        String redirectSignInUrl =
            signInPage + "?" + SecurityConst.AUTH_FAIL_PARAM_NAME + "=" + request.getAttribute(SecurityConst.AUTH_FAIL_PARAM_NAME);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            log.debug("authentication when request to: {}", targetUrl);
            List<String> headerAccepts = savedRequest.getHeaderValues(HttpHeaders.ACCEPT);
            for (String headerAccept : headerAccepts) {
                if (headerAccept.contains(MediaType.TEXT_HTML_VALUE)) {
                    redirectStrategy.sendRedirect(request, response, redirectSignInUrl);
                }
            }
            // 返回 json 响应
            return new RestResult(CommonErrorCodeEnum.AUTH_401_NEED_AUTH);

        }
        if (request.getHeader(HttpHeaders.ACCEPT).contains(MediaType.TEXT_HTML_VALUE)) {
            // 可以接受 html 的响应，跳转到指定的登录认证页面
            redirectStrategy.sendRedirect(request, response, redirectSignInUrl);
        }
        // 返回 json 响应
        return new RestResult(CommonErrorCodeEnum.AUTH_401_NEED_AUTH);
    }


}
