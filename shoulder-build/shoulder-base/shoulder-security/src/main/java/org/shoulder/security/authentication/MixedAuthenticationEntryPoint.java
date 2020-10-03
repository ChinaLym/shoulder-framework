package org.shoulder.security.authentication;

import org.shoulder.core.util.ServletUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 混合的 401 响应处理器
 * 页面访问触发时，调用页面跳转
 * 接口访问时，返回 json 响应
 *
 * @author lym
 */
public class MixedAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private AuthenticationEntryPoint pageAuthenticationEntryPoint;

    private AuthenticationEntryPoint jsonAuthenticationEntryPoint;

    public MixedAuthenticationEntryPoint(AuthenticationEntryPoint pageAuthenticationEntryPoint, AuthenticationEntryPoint jsonAuthenticationEntryPoint) {
        this.pageAuthenticationEntryPoint = pageAuthenticationEntryPoint;
        this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (ServletUtil.isBrowserPage(request)) {
            pageAuthenticationEntryPoint.commence(request, response, authException);
        } else {
            jsonAuthenticationEntryPoint.commence(request, response, authException);
        }
    }

}
