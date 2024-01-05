package org.shoulder.security.authentication.handler.mixed;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.util.ServletUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import java.io.IOException;

/**
 * 混合的 403 响应处理器
 * 页面访问触发时，调用页面跳转
 * 接口访问时，返回 json 响应
 *
 * @author lym
 */
public class MixedAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 页面处理器
     *
     * @see AccessDeniedHandlerImpl
     */
    private AccessDeniedHandler pageAccessDeniedHandler;

    /**
     * json 处理器
     *
     * @see AccessDeniedHandlerImpl
     */
    private AccessDeniedHandler jsonAccessDeniedHandler;

    public MixedAccessDeniedHandler(AccessDeniedHandler pageAccessDeniedHandler, AccessDeniedHandler jsonAccessDeniedHandler) {
        this.pageAccessDeniedHandler = pageAccessDeniedHandler;
        this.jsonAccessDeniedHandler = jsonAccessDeniedHandler;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (ServletUtil.isBrowserPage(request)) {
            pageAccessDeniedHandler.handle(request, response, accessDeniedException);
        } else {
            jsonAccessDeniedHandler.handle(request, response, accessDeniedException);
        }
    }

}
