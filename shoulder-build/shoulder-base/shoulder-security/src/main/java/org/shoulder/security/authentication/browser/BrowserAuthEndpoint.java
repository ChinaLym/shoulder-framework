package org.shoulder.security.authentication.browser;

import org.shoulder.security.ResponseUtil;
import org.shoulder.security.SecurityConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认的 待认证请求处理器
 *
 * @author lym
 */
@FrameworkEndpoint
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
     */
    @RequestMapping(SecurityConst.URL_REQUIRE_AUTHENTICATION)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public String requireAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        // 获取引发跳转的请求
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            log.debug("authentication when request to: {}", targetUrl);
            // 引发跳转的请求是否可以接受 html 的响应
            if (request.getHeader(HttpHeaders.ACCEPT).contains(MediaType.TEXT_HTML_VALUE)) {
                // 跳转到指定的登录认证页面
                redirectStrategy.sendRedirect(request, response, signInPage);
            }
        }
        return ResponseUtil.jsonMsg("the page that accessed need authentication, please guide users to the login page");
    }


}
