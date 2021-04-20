package org.shoulder.core.i18;

import org.springframework.context.i18n.TimeZoneAwareLocaleContext;

import java.nio.charset.Charset;

/**
 * 在 spring 标准多语言上下文基础上，额外支持了字符集
 *
 * @author lym
 */
@Deprecated
public interface BaseLocaleContext extends TimeZoneAwareLocaleContext {

    /**
     * 获取当地字符集
     *
     * @return 字符集
     */
    Charset getCharset();

}
