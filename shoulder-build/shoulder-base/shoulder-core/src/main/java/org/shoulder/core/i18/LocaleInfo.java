package org.shoulder.core.i18;

import jakarta.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.shoulder.core.context.AppInfo;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 地区、字符编码
 * 货币、日历、月份、数字、星期、时间周期、时间单位
 * 排序规则、单复数、省略号...
 * ISO、BCP 47
 * 注意：该类设计上期望不应序列化，不推荐放到 session，若放置推荐转为 DTO 放置
 *
 * @author lym
 */
@Getter
@EqualsAndHashCode
public final class LocaleInfo implements BaseLocaleContext, Cloneable {

    private static final LocaleInfo SYSTEM_DEFAULT;

    private static LocaleInfo DEFAULT;

    static {
        SYSTEM_DEFAULT = new LocaleInfo(Locale.getDefault(), TimeZone.getDefault(), Charset.defaultCharset());
        DEFAULT = SYSTEM_DEFAULT;
    }

    // -------------- Field --------------------------

    @Nonnull
    private final Locale locale;

    @Nonnull
    private final TimeZone timeZone;

    @Nonnull
    private final Charset charset;

    // todo 【国际化】可考虑为每种语言独立设置
    @Nonnull
    private final String dateTimeFormat = AppInfo.dateTimeFormat();

    public LocaleInfo(@NonNull Locale locale, @NonNull TimeZone timeZone, @NonNull Charset charset) {
        this.locale = locale;
        this.timeZone = timeZone;
        this.charset = charset;
    }

    // -----------------------------------------------

    @Nonnull
    public static LocaleInfo getSystemDefault() {
        return SYSTEM_DEFAULT;
    }

    @Nonnull
    public static LocaleInfo getDefault() {
        return DEFAULT;
    }

    public static void setDefault(Locale locale) {
        // charset 会随着 locale 改变而发生变化，因此使用 null
        setDefault(LocaleUtils.buildLocaleInfo(locale, DEFAULT.getTimeZone(), null));
    }

    public static void setDefault(Locale locale, Charset charset) {
        setDefault(LocaleUtils.buildLocaleInfo(locale, DEFAULT.getTimeZone(), charset));
    }

    public static void setDefault(LocaleInfo localeInfo) {
        DEFAULT = localeInfo == null ? SYSTEM_DEFAULT : localeInfo;
    }

    public static void resetDefault() {
        DEFAULT = SYSTEM_DEFAULT;
    }

    // --------------------------------------

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("locale", locale)
                .append("charset", charset)
                .append("timeZone", timeZone)
                .toString();
    }
}
