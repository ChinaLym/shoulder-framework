package org.shoulder.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.invoke.MethodHandle;

/**
 * 阻止浏览器缓存接口返回值【适用于非查询类的管理 web 应用，达到展示实时数据的效果，默认不生效】
 *
 * @author lym
 */
@Component
public class ApiResponseNoCacheInterceptor implements HandlerInterceptor {

    /**
     * 抛异常也要执行，因为异常返回值也不能缓存
     */
    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception e) {
        if (handler instanceof MethodHandle) {
            addNoCacheResponseHeaders(resp);
        }
    }

    /**
     * 为响应加上禁止缓存的标识
     */
    private static void addNoCacheResponseHeaders(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("expires", -1);
    }

}
