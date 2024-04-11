package org.shoulder.web.interceptor;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.context.ShoulderContextKey;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.log.ShoulderLoggers;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

/**
 * 默认将 http header 中的 Accept-Language 中权重最高的作为当前的 locale（如 zh-cn,zh;q=0.5）
 * <p>
 * “;”之前的类型表示的内容（'*'表示任意语言）。q 是权重，越大（为空则默认为1），请求越希望获得接收这种语言。q为0，则代表不希望接收的内容类型。
 * 完整的语言标签（如 zh-cn,zh;q=0.5），除了语言之外，还会在中划线（"-"）后面包含国家/地区变种/所用的字母系统，如"en-US"、sr-Lat"
 * <p>
 * https://jingyan.baidu.com/article/375c8e19770f0e25f2a22900.html
 *
 * @author lym
 */
public class HttpLocaleInterceptor implements AsyncHandlerInterceptor {

    private static final Logger log = ShoulderLoggers.SHOULDER_WEB;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // request.getLocale() 与具体实现有关，spring、tomcat的实现都是，若无法从请求中获取，则返回系统默认语言，必定不为空
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
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) {
        AppContext.remove(ShoulderContextKey.LOCALE);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        AppContext.remove(ShoulderContextKey.LOCALE);
    }

}
