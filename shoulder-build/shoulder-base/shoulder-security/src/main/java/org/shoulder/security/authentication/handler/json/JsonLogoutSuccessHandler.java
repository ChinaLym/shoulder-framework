package org.shoulder.security.authentication.handler.json;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.JsonUtils;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

/**
 * 返回 JSON 格式报文，表示认证已经失效
 *
 * @author lym
 */
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    private final Logger log = ShoulderLoggers.SHOULDER_SECURITY;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {

        log.debug("logout SUCCESS.");

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JsonUtils.toJson(BaseResult.error(CommonErrorCodeEnum.AUTH_401_EXPIRED)));
    }

}
