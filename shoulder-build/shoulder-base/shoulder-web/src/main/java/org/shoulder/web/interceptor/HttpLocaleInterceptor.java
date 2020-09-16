package org.shoulder.web.interceptor;

import lombok.extern.shoulder.SLog;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.context.ShoulderContextKey;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 默认将 http header 中的 Accept-Language 中权重最高的作为当前的 locale
 * <p>
 * zh-cn,zh;q=0.5  q 值越大，请求越倾向于获得其“;”之前的类型表示的内容，若没有指定 q 值，则默认为1，若被赋值为0，则用于提醒服务器哪些是浏览器不接受的内容类型。
 * "*"表示任意语言。
 * 完整的语言标签。除了语言本身之外，还会包含其他方面的信息，显示在中划线（"-"）后面。最常见的额外信息是国家或地区变种（如"en-US"）或者表示所用的字母系统（如"sr-Lat"）。
 * <p>
 * https://jingyan.baidu.com/article/375c8e19770f0e25f2a22900.html
 *
 * @author lym
 */
@SLog
public class HttpLocaleInterceptor extends HandlerInterceptorAdapter {

    /**
     * request.getLocale() 与具体实现有关
     * spring、tomcat的实现都是，若无法从请求中获取，则返回系统默认语言
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Locale locale = request.getLocale();
        if (locale == null) {
            locale = AppInfo.defaultLocale();
            log.debug("request.locale is null, use AppInfo.defaultLocale({})", locale);
        }
        AppContext.setLocale(locale);
        return true;
    }


    // =============== 清理线程变量缓存 ===================


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        AppContext.remove(ShoulderContextKey.Locale);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        AppContext.remove(ShoulderContextKey.Locale);
    }

}
