package org.shoulder.web.filter;

import jakarta.servlet.*;
import org.shoulder.core.context.AppContext;

import java.io.IOException;

/**
 * 仅为测试使用：上下文填默认User
 *
 * @author lym
 */
public class MockUserFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 执行前确保有 traceId、spanId
        AppContext.setUserId("1");
        AppContext.setUserName("MockUserFilter_Generated");
        filterChain.doFilter(servletRequest, servletResponse);
    }

}
