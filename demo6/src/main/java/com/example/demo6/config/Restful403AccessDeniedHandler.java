package com.example.demo6.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.dto.response.RestResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Token 模式 JSON 响应 认证过的用户访问无权限资源时的异常
 *
 * @author lym
 */
@Component
@Slf4j
public class Restful403AccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.debug("accessDenied 403 for: " + accessDeniedException.getMessage(), accessDeniedException);
        RestResult result = CommonErrorCodeEnum.AUTH_403_FORBIDDEN.toResponse(accessDeniedException.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

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