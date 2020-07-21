package org.shoulder.web.filter.xss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SLog
public class XssFilter implements Filter {

    /**
     * .waf需要排除的url
     */
    private XssProperties xssProperties;

    /**
     * .xss需要排除的url
     */
    @Value("${waf.xss.excludePathPatterns:}")
    private String csrfExcludePathPatterns;

    public XssFilter(XssProperties xssProperties) {
        this.xssProperties = xssProperties;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    /**
     * @param request  request
     * @param response response
     * @param chain    chain
     * @throws IOException      IOException
     * @throws ServletException ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (excludePath(xssProperties.getExcludePathPatterns(), httpServletRequest.getRequestURI())
            || excludePath(csrfExcludePathPatterns, httpServletRequest.getRequestURI())) {
            //是需要排除的url
            chain.doFilter(request, response);
        } else {
            chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * 是否需要过滤
     * @param excludePathStr
     * @param uri
     * @return
     */
    boolean excludePath(String excludePathStr, String uri) {
        AntPathMatcher matcher = new AntPathMatcher();
        String[] excludePathPatterns = excludePathStr.split(",");
        for (String excludePathPattern : excludePathPatterns) {
            if (matcher.match(excludePathPattern, uri)) {
                return true;
            }
        }
        return false;
    }

}
