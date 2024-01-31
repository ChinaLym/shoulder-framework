package org.shoulder.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * 链路追踪过滤器，确保上下文中有trace
 * 使用者可以自行实现替代
 *
 * @author lym
 */
public class TraceFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 执行前确保有 traceId、spanId
        String traceIdFromRequest = resolveTraceId(servletRequest);
        if (StringUtils.isBlank(traceIdFromRequest)) {
            AppContext.setTranceId(UUID.randomUUID().toString());
        } else {
            AppContext.setTranceId(traceIdFromRequest);
        }
        filterChain.doFilter(servletRequest, servletResponse);

    }

    private String resolveTraceId(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest httpServletRequest) {
            return httpServletRequest.getHeader("X-B3-TraceId");
        }
        return null;
    }
}
