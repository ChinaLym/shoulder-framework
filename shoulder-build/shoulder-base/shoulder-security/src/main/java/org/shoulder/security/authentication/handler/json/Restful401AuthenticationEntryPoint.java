package org.shoulder.security.authentication.handler.json;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.JsonUtils;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 匿名用户访问无权限资源时的异常
 *
 * @author lym
 */
public class Restful401AuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Logger log = ShoulderLoggers.SHOULDER_SECURITY;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.info("need auth 401 for: " + authException.getMessage(), authException);

        BaseResult<Void> result = BaseResult.error(CommonErrorCodeEnum.AUTH_401_EXPIRED);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(AppInfo.charset().toString());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String resBody = JsonUtils.toJson(result);
        PrintWriter printWriter = response.getWriter();
        printWriter.print(resBody);
        printWriter.flush();
        printWriter.close();
    }

}
