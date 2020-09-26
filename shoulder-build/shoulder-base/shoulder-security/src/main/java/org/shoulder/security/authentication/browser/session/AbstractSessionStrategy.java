package org.shoulder.security.authentication.browser.session;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.core.util.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 抽象的session失效处理器
 *
 * @author lym
 */
public class AbstractSessionStrategy {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 跳转的url
     */
    private String sessionInvalidUrl;
    /**
     * 登录页面地址
     */
    private String signInPage;
    /**
     * 退出登录跳转
     */
    private String signOutUrl;
    /**
     * 重定向策略
     */
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    /**
     * 跳转前是否创建新的session
     */
    private boolean createNewSession = true;


    public AbstractSessionStrategy(String sessionInvalidUrl, String signInPage, String signOutUrl) {
        Assert.isTrue(UrlUtils.isValidRedirectUrl(sessionInvalidUrl), "url must start with '/' or with 'http(s)'");
        //Assert.isTrue(StringUtils.endsWithIgnoreCase(invalidSessionUrl, ".html"), "url must end with '.html'");
        this.sessionInvalidUrl = sessionInvalidUrl;
        this.signInPage = signInPage;
        this.signOutUrl = signOutUrl;
    }

    protected void onSessionInvalid(HttpServletRequest request, HttpServletResponse response) throws IOException {

        this.logger.debug("Starting new session (if required) and redirecting to '" + this.sessionInvalidUrl + "'");
        if (this.createNewSession) {
            request.getSession();
        }

        String sourceUrl = request.getRequestURI();
        String targetUrl;

        // 如果请求页面。则跳转至登录
        if (isPageRequest(request)) {
            if (StringUtils.equals(sourceUrl, signInPage)
                || StringUtils.equals(sourceUrl, signOutUrl)) {
                targetUrl = sourceUrl;
            } else {
                targetUrl = sessionInvalidUrl;
            }
            logger.debug("redirectTo:" + targetUrl);
            redirectStrategy.sendRedirect(request, response, targetUrl);
        } else {
            RestResult needAuthResponse = new RestResult(CommonErrorCodeEnum.AUTH_401_NEED_AUTH);
            String resultMsg = buildResponseContent(request);
            needAuthResponse.setMsg(resultMsg);
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().write(JsonUtils.toJson(needAuthResponse));
        }

    }

    /**
     * 请求的是否为页面，暂且认为非ajax就是页面请求。若前后分离则总是返回 false
     */
    protected boolean isPageRequest(HttpServletRequest request) {
        return !ServletUtil.isAjax(request) && request.getHeader(HttpHeaders.ACCEPT).contains(MediaType.TEXT_HTML_VALUE);
    }

    protected String buildResponseContent(HttpServletRequest request) {
        StringBuilder result = new StringBuilder("session invalid");
        if (isConcurrency()) {
            result.append(",it may caused by concurrent logIn.");
        }
        return result.toString();
    }

    /**
     * session失效是否是并发导致的
     */
    protected boolean isConcurrency() {
        return false;
    }

    public void setCreateNewSession(boolean createNewSession) {
        this.createNewSession = createNewSession;
    }

}
