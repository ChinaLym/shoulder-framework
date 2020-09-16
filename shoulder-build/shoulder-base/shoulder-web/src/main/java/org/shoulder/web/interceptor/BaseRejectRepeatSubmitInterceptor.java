package org.shoulder.web.interceptor;

import lombok.extern.shoulder.SLog;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 防止重复提交拦截器
 *
 * @author lym
 */
@SLog
public abstract class BaseRejectRepeatSubmitInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (needIntercept(request, handler) && isRepeatSubmit(request)) {
            // 重复提交，拒绝执行
            handleReject(request, response);
            return false;
        }
        // 允许执行，清理服务端 token
        cleanServerToken(request);
        return true;
    }

    /**
     * 是否为重复提交
     *
     * @param request 请求
     * @return 是否为重复提交
     */
    private boolean isRepeatSubmit(HttpServletRequest request) {
        Object serverToken = getServerToken(request);
        Object clientToken = getClientToken(request);
        return serverToken != null && serverToken.equals(clientToken);
    }

    /**
     * 是否需要拦截该处理器
     *
     * @param request 请求
     * @param handler 处理器
     * @return 是否需要拦截
     */
    protected abstract boolean needIntercept(HttpServletRequest request, Object handler);

    /**
     * 服务端 token
     *
     * @param request 请求
     * @return token
     */
    protected abstract Object getServerToken(HttpServletRequest request);

    /**
     * 清理服务端 token
     *
     * @param request 请求
     */
    protected abstract void cleanServerToken(HttpServletRequest request);

    /**
     * 客户端 token
     *
     * @param request 请求
     * @return token
     */
    protected abstract Object getClientToken(HttpServletRequest request);

    /**
     * 处理被拒绝的请求：默认只记录一条debug日志
     *
     * @param request   请求
     * @param response  响应
     */
    protected void handleReject(HttpServletRequest request, HttpServletResponse response) {
        log.debug("reject repeatSubmit request({})", request.getRequestURI());
    }

}
