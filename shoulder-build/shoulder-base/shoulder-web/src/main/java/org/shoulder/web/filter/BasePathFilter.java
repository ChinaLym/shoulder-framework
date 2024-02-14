package org.shoulder.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;


/**
 * 通用过滤器
 * 实现了是否需要过滤当前路径的判断
 *
 * @author lym
 */
public abstract class BasePathFilter implements Filter {

    private final PathFilterHelper pathFilterhelper;

    public BasePathFilter(PathFilterHelper pathFilterhelper) {
        this.pathFilterhelper = pathFilterhelper;

    }

    /**
     * filter 过滤方法
     *
     * @implSpec 子类必须至少实现本方法 或 {@link #doPathFilter} 两个之一
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        if (pathFilterhelper.needFilter(httpServletRequest.getRequestURI())) {
            doPathFilter(httpServletRequest, httpServletResponse, filterChain);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * 筛选过路径的过滤器
     *
     * @param request     request
     * @param response    response
     * @param filterChain chain
     * @throws IOException      IOException
     * @throws ServletException ServletException
     * @implSpec 子类必须至少实现本方法 或 {@link #doFilter} 两个之一
     */
    protected void doPathFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

    }

    /**
     * 通过该方法添加需要过滤的路径
     */
    protected void addFilterPathPattern(List<String> filterPathPattern) {
        this.pathFilterhelper.addFilterPathPattern(filterPathPattern);
    }

    /**
     * 通过该方法添加不需要过滤的路径
     */
    protected void addExcludePathPattern(List<String> excludePathPattern) {
        this.pathFilterhelper.addExcludePathPattern(excludePathPattern);
    }

    public boolean isEnable() {
        return this.pathFilterhelper.isEnable();
    }

    /**
     * 子类可以调用方法实现脱离全局开关的控制
     */
    protected void setEnable(boolean enable) {
        this.pathFilterhelper.setEnable(enable);
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
