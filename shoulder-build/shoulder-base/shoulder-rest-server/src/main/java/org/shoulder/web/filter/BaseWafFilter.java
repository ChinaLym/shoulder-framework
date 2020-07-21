package org.shoulder.web.filter;

import org.shoulder.web.BaseWafProperties;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * 通用过滤器
 * @author lym
 */
public class BaseWafFilter implements Filter {

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
     * 配置信息
     */
    protected BaseWafProperties baseWafProperties;

    public BaseWafFilter(BaseWafProperties baseWafProperties){
        this.baseWafProperties = baseWafProperties;
        this.excludePaths = new LinkedList<>();
        baseWafProperties.getExcludePathPatterns()
    }

    /**
     * 添加需要过滤的路径
     */
    protected void addFilterPathPattern(String filterPathPattern){
        this.filterPaths.add(filterPathPattern);
    }

    /**
     * 添加不需要过滤的路径
     */
    protected void addExcludPathPattern(String excludePathPattern){
        this.excludePaths.add(excludePathPattern);
    }

    protected boolean enable(){
        return baseWafProperties.getEnable();
    }

    /**
     * 判断 url 是否需要进行过滤
     */
    protected boolean needFilter(String uri){
        String[] excludePathPatterns = excludePathStr.split(",");
        for (String excludePathPattern : excludePathPatterns) {
            if (matcher.match(excludePathPattern, uri)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    }

    @Override
    public void destroy() {

    }
}
