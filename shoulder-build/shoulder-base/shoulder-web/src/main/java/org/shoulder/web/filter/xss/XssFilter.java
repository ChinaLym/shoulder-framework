package org.shoulder.web.filter.xss;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.web.filter.BasePathFilter;
import org.shoulder.web.filter.PathFilterHelper;
import org.shoulder.web.filter.PathFilterProperties;

import java.io.IOException;

/**
 * xss 过滤器
 * 保护用户防止XSS攻击：用户A填写内容中包含 script 等 html 标签，用户 B 在查看这些内容时被浏览器意外执行而受到攻击
 *
 * @author lym
 */
public class XssFilter extends BasePathFilter {

    public XssFilter(PathFilterProperties xssProperties) {
        super(new PathFilterHelper(xssProperties));
    }

    @Override
    protected void doPathFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(new XssRequestWrapper(request), response);
    }


}
