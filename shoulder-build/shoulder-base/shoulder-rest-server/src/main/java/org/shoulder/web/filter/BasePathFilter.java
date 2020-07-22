package org.shoulder.web.filter;

import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * 通用过滤器
 *  实现了是否需要过滤当前路径的判断
 *
 * @author lym
 */
public class BasePathFilter implements Filter {

    /**
     * 支持 ant 匹配
     */
    protected AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 需要过滤的路径
     */
    protected List<String> filterPaths;

    /**
     * 不需要过滤的路径
     */
    protected List<String> excludePaths;

    /**
     * 是否激活此过滤器
     */
    private Boolean enable;

    public BasePathFilter(AbstractPathFilterProperties pathFilterProperties){
        this.filterPaths = new LinkedList<>();
        this.excludePaths = new LinkedList<>();
        initProperties(pathFilterProperties);

    }

    /**
     * filter 过滤方法
     * @implSpec @implSpec 子类必须至少实现 {@link #doFilter} 或 {@link #doPathFilter} 两个方法中的一个
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        if (needFilter(httpServletRequest.getRequestURI())) {
            doPathFilter(httpServletRequest, httpServletResponse, filterChain);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * 筛选过路径的过滤器
     * @param request  request
     * @param response response
     * @param filterChain    chain
     * @throws IOException      IOException
     * @throws ServletException ServletException
     *
     * @implSpec 子类必须至少实现 {@link #doFilter} 或 {@link #doPathFilter} 两个方法中的一个
     */
    protected void doPathFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

    }

    /**
     * 子类可以覆盖该方法实现脱离全局开关的控制
     */
    private void initProperties(AbstractPathFilterProperties properties) {
        if(properties != null){
            addFilterPathPattern(properties.getPathPatterns());
            addExcludePathPattern(properties.getExcludePathPatterns());
            this.enable = properties.getEnable();
        }
    }

    /**
     * 通过该方法添加需要过滤的路径
     */
    protected void addFilterPathPattern(List<String> filterPathPattern){
        this.filterPaths.addAll(filterPathPattern);
    }

    /**
     * 通过该方法添加不需要过滤的路径
     */
    protected void addExcludePathPattern(List<String> excludePathPattern){
        this.excludePaths.addAll(excludePathPattern);
    }

    /**
     * 是否关闭该过滤器
     */
    protected boolean ignore(){
        return !this.enable;
    }

    /**
     * 判断 url 是否需要进行过滤，需要同时满足三个条件
     *  开启过滤器
     *  在校验路径中
     *  不在排除路径中
     */
    protected boolean needFilter(String uri){
        if(ignore()){
            return false;
        }
        for (String filterPath : filterPaths) {
            if (matcher.match(filterPath, uri)) {
                // 满足需要校验路径的条件
                for (String excludePath : excludePaths){
                    if(matcher.match(excludePath, uri)){
                        // 需要排除/跳过/忽略
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
