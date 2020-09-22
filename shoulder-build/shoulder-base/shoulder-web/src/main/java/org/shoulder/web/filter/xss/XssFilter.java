package org.shoulder.web.filter.xss;

import org.shoulder.web.filter.BasePathFilter;
import org.shoulder.web.filter.PathFilterHelper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * xss 过滤器
 *
 * @author lym
 */
public class XssFilter extends BasePathFilter {

    public XssFilter(XssProperties xssProperties) {
        super(new PathFilterHelper(xssProperties));
    }

    @Override
    protected void doPathFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(new XssRequestWrapper(request), response);
    }


}
