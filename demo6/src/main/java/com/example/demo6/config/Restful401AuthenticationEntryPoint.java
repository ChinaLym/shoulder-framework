package com.example.demo6.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.shoulder.SLog;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 匿名用户访问无权限资源时的异常
 *
 * @author lym
 */
@Component
@SLog
public class Restful401AuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        log.debug("auth fail 401", authException);
        RestResult result = CommonErrorCodeEnum.AUTH_401_NEED_AUTH.toResponse();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setCharacterEncoding(AppInfo.charset().toString());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        String resBody = objectMapper.writeValueAsString(result);
        PrintWriter printWriter = response.getWriter();
        printWriter.print(resBody);
        printWriter.flush();
        printWriter.close();
    }

}