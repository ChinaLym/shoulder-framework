package org.shoulder.web;

import org.shoulder.core.i18.BaseLocaleContext;
import org.shoulder.core.i18.LocaleInfo;
import org.shoulder.core.i18.LocaleUtils;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 在 DispatcherServlet 基础的解析多语言方法上额外增加了 charset
 * 单测暂未实现相关能力
 *
 * @author lym
 */
public class ShoulderDispatcherServlet extends DispatcherServlet {

    private static final long serialVersionUID = -8413315383105362540L;

    public ShoulderDispatcherServlet() {
        super();
    }

    public ShoulderDispatcherServlet(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
    }

    /**
     * 重写了 DispatcherServlet 的解析多语言方法，在其基础上额外增加了 charset
     *
     * @param request 请求
     * @return 本地化信息
     * @see BaseLocaleContext
     */
    @Override
    protected LocaleContext buildLocaleContext(@Nonnull final HttpServletRequest request) {
        LocaleContext origin = super.buildLocaleContext(request);
        Locale locale = origin != null ? origin.getLocale() : LocaleInfo.getDefault().getLocale();
        TimeZone timeZone = LocaleInfo.getDefault().getTimeZone();
        Charset charset = LocaleInfo.getDefault().getCharset();
        if (origin instanceof TimeZoneAwareLocaleContext) {
            timeZone = ((TimeZoneAwareLocaleContext) origin).getTimeZone();
        }
        if (origin instanceof TimeZoneAwareLocaleContext) {
            timeZone = ((TimeZoneAwareLocaleContext) origin).getTimeZone();
        }
        return LocaleUtils.buildLocaleInfo(locale, timeZone, charset);
    }

}
