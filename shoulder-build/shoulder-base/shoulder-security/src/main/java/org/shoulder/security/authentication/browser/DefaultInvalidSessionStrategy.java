package org.shoulder.security.authentication.browser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.session.InvalidSessionStrategy;

import java.io.IOException;

/**
 * 默认的 session 无效处理策略
 * （第一次访问不带sessionid必定无效）
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
