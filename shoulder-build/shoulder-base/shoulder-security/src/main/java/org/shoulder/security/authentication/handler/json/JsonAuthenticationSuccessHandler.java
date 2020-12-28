package org.shoulder.security.authentication.handler.json;

import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证成功后，返回 Json 响应
 *
 * @author lym
 */
public class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static final String SUCCESS_RESPONSE = JsonUtils.toJson(RestResult.success());

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.debug("login success");
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(SUCCESS_RESPONSE);
    }
}
