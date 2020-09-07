package org.shoulder.security.authentication.browser.session;

import org.springframework.security.web.session.InvalidSessionStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 默认的 session 无效处理策略
 * （第一次访问必定无效）
 *
 * @author lym
 */
public class DefaultInvalidSessionStrategy extends AbstractSessionStrategy implements InvalidSessionStrategy {

    public DefaultInvalidSessionStrategy(String sessionInvalidUrl, String signInPage, String signOutUrl) {
        super(sessionInvalidUrl, signInPage, signOutUrl);
    }

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        onSessionInvalid(request, response);
    }

}
