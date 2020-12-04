package org.shoulder.security.authentication.handler.json;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.security.AuthResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证失败后返回 Json 响应
 *
 * @author lym
 */
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.info("authentication FAIL.", exception);
        AuthResponseUtil.authFail(response, exception, CommonErrorCodeEnum.AUTH_401_UNAUTHORIZED);
    }

}
