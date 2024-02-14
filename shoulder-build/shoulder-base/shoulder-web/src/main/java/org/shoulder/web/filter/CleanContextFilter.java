package org.shoulder.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.shoulder.core.context.AppContext;

import java.io.IOException;

/**
 * 清空上下文过滤器
 *
 * @author lym
 */
public class CleanContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 执行前清理初始化
        AppContext.clean();
        try {
            // 执行请求
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            // 执行后清理
            AppContext.clean();
        }
    }
}
