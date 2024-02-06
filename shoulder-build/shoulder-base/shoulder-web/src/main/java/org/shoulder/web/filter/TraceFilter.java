package org.shoulder.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final String TRACE_ID = "X-B3-TraceId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 执行前确保有 traceId、spanId
        String traceIdFromRequest = resolveTraceId(servletRequest);
        if (StringUtils.isBlank(traceIdFromRequest)) {
            AppContext.setTraceId(UUID.randomUUID().toString());
        } else {
            AppContext.setTraceId(traceIdFromRequest);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            setTraceIdToResponse(servletResponse);
        }
    }

    private String resolveTraceId(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest httpServletRequest) {
            return httpServletRequest.getHeader(TRACE_ID);
        }
        return null;
    }

    private void setTraceIdToResponse(ServletResponse servletResponse) {
        String traceId;
        if (servletResponse instanceof HttpServletResponse httpServletResponse && (traceId = AppContext.getTraceId()) != null) {
            if(StringUtils.isEmpty(httpServletResponse.getHeader(TRACE_ID))) {
                httpServletResponse.setHeader(TRACE_ID, traceId);
            }
        }
    }
}
