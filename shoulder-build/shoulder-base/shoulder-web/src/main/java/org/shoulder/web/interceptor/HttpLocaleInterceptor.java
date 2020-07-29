package org.shoulder.web.interceptor;

import org.shoulder.core.context.BaseContextHolder;
import org.shoulder.core.context.ShoulderContextKey;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 默认将 http header 中的 Accept-Language 中权重最高的作为当前的 locale
 *
 * zh-cn,zh;q=0.5  q 值越大，请求越倾向于获得其“;”之前的类型表示的内容，若没有指定 q 值，则默认为1，若被赋值为0，则用于提醒服务器哪些是浏览器不接受的内容类型。　
 * "*"表示任意语言。
 * 完整的语言标签。除了语言本身之外，还会包含其他方面的信息，显示在中划线（"-"）后面。最常见的额外信息是国家或地区变种（如"en-US"）或者表示所用的字母系统（如"sr-Lat"）。
 *
 * https://jingyan.baidu.com/article/375c8e19770f0e25f2a22900.html
 *
 * @author lym
 */
public class HttpLocaleInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        BaseContextHolder.setLocale(request.getLocale());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        BaseContextHolder.remove(ShoulderContextKey.Locale);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        BaseContextHolder.remove(ShoulderContextKey.Locale);
    }

}
