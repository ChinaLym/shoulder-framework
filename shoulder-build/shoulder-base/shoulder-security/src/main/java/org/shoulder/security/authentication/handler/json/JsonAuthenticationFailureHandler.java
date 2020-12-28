package org.shoulder.security.authentication.handler.json;

import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
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
        RestResult restResult = RestResult.error(CommonErrorCodeEnum.AUTH_401_UNAUTHORIZED, exception.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(JsonUtils.toJson(restResult));
    }

}
