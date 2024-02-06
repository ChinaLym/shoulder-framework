package org.shoulder.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.shoulder.core.context.AppContext;

import java.io.IOException;

/**
 * 上下文填默认 tenantId
 *
 * @author lym
 */
public class DefaultTenantFilter implements Filter {

    private final String defaultTenantName;

    public DefaultTenantFilter(String defaultTenantName) {this.defaultTenantName = defaultTenantName;}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 执行前确保有 traceId、spanId
        AppContext.setTenantCode(defaultTenantName);
        filterChain.doFilter(servletRequest, servletResponse);
    }

}
