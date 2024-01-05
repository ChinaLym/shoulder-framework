package org.shoulder.security.authentication.browser;

import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;

/**
 * 达到并发登录限制上线导致session失效时，默认的处理策略
 *
 * @author lym
 */
public class ConcurrentLogInExpiredSessionStrategy extends AbstractSessionStrategy implements SessionInformationExpiredStrategy {

    public ConcurrentLogInExpiredSessionStrategy(String sessionInvalidUrl, String signInPage, String signOutUrl) {
        super(sessionInvalidUrl, signInPage, signOutUrl);
    }

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        onSessionInvalid(event.getRequest(), event.getResponse());
    }

    @Override
    protected boolean isConcurrency() {
        return true;
    }

}
