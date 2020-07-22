package org.shoulder.web.interceptor;

import org.shoulder.web.annotation.RejectRepeatSubmit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

/**
 * 基于Session和表单token的防止重复提交拦截器
 *
 * @author lym
 */
public class SessionTokenRepeatSubmitInterceptor extends BaseRejectRepeatSubmitInterceptor {

    /**
     * 用于重复提交校验的 token 名
     */
    @Value("${shoulder.web.waf.repeatSubmit.requestTokenName:__repeat_token}")
    private String requestTokenName;

    @Value("${shoulder.web.waf.repeatSubmit.sessionTokenName:__repeat_token}")
    private String sessionTokenName;

    /**
     * 是否开启校验，开发时可以关闭
     */
    @Value("${shoulder.web.repeatSubmit.enable:true}")
    private boolean enable;

    @Override
    protected boolean needIntercept(HttpServletRequest request, Object handler) {
        if (enable || handler instanceof HandlerMethod) {
            // 当前仅当开启校验，且为 HandlerMethod 才拦截
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            // 且目标方法上必须有 RejectRepeatSubmit 注解才拦截
            return method.getAnnotation(RejectRepeatSubmit.class) != null;
        }
        return false;
    }

    /**
     * 服务端 token
     * @param request 请求
     * @return token
     */
    @Override
    protected Object getServerToken(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if (session == null) {
            // 没有 session
            return null;
        }
        return session.getAttribute(sessionTokenName);
    }

    @Override
    protected void cleanServerToken(HttpServletRequest request){
        request.getSession(false).removeAttribute(sessionTokenName);
    }

    /**
     * 客户端 token
     * @param request 请求
     * @return token
     */
    @Override
    protected Object getClientToken(HttpServletRequest request){
        return request.getParameter(requestTokenName);
    }

    @Override
    protected void handleReject(HttpServletRequest request, HttpServletResponse response){

    }

}
