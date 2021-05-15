package org.shoulder.autoconfigure.operation;

import org.shoulder.log.operation.context.OpLogContext;
import org.shoulder.log.operation.model.Operator;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 当前用户信息解析，操作日志的拦截器
 *
 * @author lym
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public abstract class OperationLogOperatorInfoInterceptor implements HandlerInterceptor {

    /**
     * 通过 request 解析当前操作者信息
     * 用该方法的返回值作为 OperationLogBuilder 创建操作日志实体时操作者信息的默认值
     *
     * @param request 当前请求
     * @return 作为创建日志实体的默认值
     */
    protected abstract Operator resolveOperator(HttpServletRequest request);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        OpLogContext.setDefaultOperator(resolveOperator(request));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        OpLogContext.cleanDefaultOperator();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        OpLogContext.cleanDefaultOperator();
    }

}
