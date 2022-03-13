package org.shoulder.web.filter;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.LinkedList;
import java.util.List;


/**
 * 路径过滤器，代理模式工具类
 *
 * @author lym
 */
public class PathFilterHelper {

    /**
     * 支持 ant 匹配
     * 如果不用类似 |**|health 这种后缀匹配, PathPatternParser 性能会高一些
     */
    protected PathMatcher pathMatcher = new AntPathMatcher();

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
    private boolean enable = true;

    public PathFilterHelper(PathFilterProperties properties) {
        this.filterPaths = new LinkedList<>();
        this.excludePaths = new LinkedList<>();
        if (properties != null) {
            addFilterPathPattern(properties.getPathPatterns());
            addExcludePathPattern(properties.getExcludePathPatterns());
            this.enable = properties.getEnable();
        }

    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * 通过该方法添加需要过滤的路径
     */
    protected void addFilterPathPattern(List<String> filterPathPattern) {
        this.filterPaths.addAll(filterPathPattern);
    }

    /**
     * 通过该方法添加不需要过滤的路径
     */
    protected void addExcludePathPattern(List<String> excludePathPattern) {
        this.excludePaths.addAll(excludePathPattern);
    }


    /**
     * 判断 url 是否需要进行过滤，需要同时满足三个条件
     * 开启过滤器
     * 在校验路径中
     * 不在排除路径中
     */
    public boolean needFilter(String uri) {
        if (!this.enable) {
            return false;
        }
        for (String filterPath : filterPaths) {
            if (pathMatcher.match(filterPath, uri)) {
                // 满足需要校验路径的条件
                for (String excludePath : excludePaths) {
                    if (pathMatcher.match(excludePath, uri)) {
                        // 需要排除/跳过/忽略
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

}
