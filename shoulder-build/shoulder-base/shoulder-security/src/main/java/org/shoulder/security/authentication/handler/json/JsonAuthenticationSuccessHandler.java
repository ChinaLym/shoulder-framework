package org.shoulder.security.authentication.handler.json;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * 认证成功后，返回 Json 响应
 *
 * @author lym
 */
public class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final Logger log = ShoulderLoggers.SHOULDER_SECURITY;

    private static final char[] SUCCESS_RESPONSE = JsonUtils.toJson(BaseResult.success()).toCharArray();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.debug("login success");
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(SUCCESS_RESPONSE);
    }
}
