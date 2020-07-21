package org.shoulder.security.authentication.browser.session;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.security.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

        if (StringUtils.endsWithIgnoreCase(sourceUrl, ".html")) {
            if (StringUtils.equals(sourceUrl, signInPage)
                    || StringUtils.equals(sourceUrl, signOutUrl)) {
                targetUrl = sourceUrl;
            } else {
                targetUrl = sessionInvalidUrl;
            }
            logger.debug("redirectTo:" + targetUrl);
            redirectStrategy.sendRedirect(request, response, targetUrl);
        } else {
            String result = buildResponseContent(request);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().write(ResponseUtil.jsonMsg(result));
        }

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
