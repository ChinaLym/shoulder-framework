package org.shoulder.web.interceptor;

import org.shoulder.web.annotation.RejectRepeatSubmit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

/**
 * 防止重复提交拦截器
 *
 * @author lym
 */
public class RejectRepeatSubmitInterceptor extends HandlerInterceptorAdapter {

    /**
     * 用于重复提交校验的 token 名
     */
    @Value("${shoulder.web.repeatSubmit.requestTokenName:__repeat_token}")
    private String requestTokenName;

    @Value("${shoulder.web.repeatSubmit.sessionTokenName:__repeat_token}")
    private String sessionTokenName;

    /**
     * 是否开启校验，开发时可以关闭
     */
    @Value("${shoulder.web.repeatSubmit.enable:true}")
    private boolean enable;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return super.preHandle(request, response, handler);
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        RejectRepeatSubmit annotation = method.getAnnotation(RejectRepeatSubmit.class);
        if (annotation == null) {
            // 未加注解不拦截
            return super.preHandle(request, response, handler);
        }
        if (isRepeatSubmit(request)) {
            return false;
        }
        request.getSession(false).removeAttribute(sessionTokenName);
        return true;

    }

    private boolean isRepeatSubmit(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }
        String serverToken = (String) session.getAttribute(sessionTokenName);
        if (serverToken == null) {
            return true;
        }
        String clientToken = request.getParameter(sessionTokenName);
        return !serverToken.equals(clientToken);
    }

}
