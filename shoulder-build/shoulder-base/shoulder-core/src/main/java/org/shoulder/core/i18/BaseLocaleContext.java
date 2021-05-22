package org.shoulder.core.i18;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;

import java.nio.charset.Charset;

/**
 * 在 spring 标准多语言上下文基础上，额外支持了字符集
 *
 * @author lym
 * 要替换 Spring 的内部的，就需要改造 DispatcherServlet
 * @see LocaleContextHolder#setLocaleContext(org.springframework.context.i18n.LocaleContext, boolean)
 * @see org.springframework.web.servlet.FrameworkServlet#initContextHolders
 */
public interface BaseLocaleContext extends TimeZoneAwareLocaleContext {

    /**
     * 获取当地字符集
     *
     * @return 字符集
     */
    Charset getCharset();

}
