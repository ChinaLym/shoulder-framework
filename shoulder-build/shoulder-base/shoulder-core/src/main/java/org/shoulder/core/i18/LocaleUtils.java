package org.shoulder.core.i18;

import jakarta.annotation.Nullable;
import org.shoulder.core.util.StringUtils;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 地域处理工具
 *
 * @author lym
 */
public class LocaleUtils extends org.apache.commons.lang3.LocaleUtils {

    private static final CharsetMap CHARSET_MAP = new CharsetMap();

    private static final Set<String> AVAILABLE_LANGUAGES;

    private static final Set<String> AVAILABLE_COUNTRIES;

    private static final Set<String> AVAILABLE_TIME_ZONES;

    static {
        Locale[] availableLocales = Locale.getAvailableLocales();
        AVAILABLE_LANGUAGES = Arrays.stream(availableLocales).map(Locale::getLanguage).collect(Collectors.toUnmodifiableSet());
        AVAILABLE_COUNTRIES = Arrays.stream(availableLocales).map(Locale::getCountry).collect(Collectors.toUnmodifiableSet());
        AVAILABLE_TIME_ZONES = Arrays.stream(TimeZone.getAvailableIDs()).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * properties 文件中 key 不能包含 '-'/'#'/'$'，用 '_' 来分隔，这里用于还原
     */
    public static final String LANGUAGE_COUNTRY_SPLIT = "_";


    // ================================ Methods ==============================================


    /**
     * safe build
     *
     * @param locale   null 则默认值
     * @param charset  null 则根据 locale 查找
     * @param timeZone null 则默认值
     * @return LocaleInfo
     */
    public static LocaleInfo buildLocaleInfo(@Nullable Locale locale, @Nullable TimeZone timeZone, @Nullable Charset charset) {
        if (locale == null) {
            locale = LocaleInfo.getDefault().getLocale();
        }
        if (charset == null) {
            charset = CHARSET_MAP.getCharSet(locale);
        }
        if (timeZone == null) {
            timeZone = LocaleInfo.getDefault().getTimeZone();
        }
        return new LocaleInfo(locale, timeZone, charset);
    }

    public static boolean isSupportedLocale(Locale locale) {
        return (locale != null) && AVAILABLE_LANGUAGES.contains(locale.getLanguage())
                && AVAILABLE_COUNTRIES.contains(locale.getCountry());
    }

    public static boolean isSupportedCharset(String charset) {
        return Charset.isSupported(charset);
    }

    public static boolean isSupportedTimeZone(String timeZone) {
        return AVAILABLE_TIME_ZONES.contains(timeZone);
    }


    /**
     * 解析 properties 文件中的 locale 信息
     *
     * @param localeName properties 文件中的 locale 信息
     * @return Locale
     */
    @Nullable
    public static Locale parseLocale(String localeName) {
        if (localeName == null) {
            return null;
        }
        String[] localeParts = StringUtils.split(localeName, LANGUAGE_COUNTRY_SPLIT);
        int len = localeParts.length;

        if (len == 0) {
            return null;
        }
        String language = localeParts[0];

        String country = "";
        String variant = "";
        if (len > 1) {
            country = localeParts[1];
        }
        if (len > 2) {
            variant = localeParts[2];
        }
        return new Locale(language, country, variant);
    }

    public static String getCharset(String charset, String defaultCharset) {
        String result = null;
        try {
            result = Charset.forName(charset).name();
        } catch (IllegalArgumentException e) {
            if (defaultCharset != null) {
                try {
                    result = Charset.forName(charset).name();
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return result;
    }


    public static List<String> calculateBundleNames(String baseName, Locale locale) {
        return calculateBundleNames(baseName, locale, false);
    }

    /**
     * 计算国际化后的资源名称（需要倒序优先遍历）
     * hello.world
     * hello.world_zh
     * hello.world_zh_CN
     * hello.world_zh_CN_variant
     *
     * @param useExt true 在文件名后扩展名前拼接；false 直接在 baseName 后拼接
     */
    public static List<String> calculateBundleNames(String baseName, Locale locale, boolean useExt) {
        baseName = StringUtils.defaultString(baseName);

        if (locale == null) {
            locale = new Locale("");
        }

        String ext = "";
        int extLength = 0;

        if (useExt) {
            int extIndex = baseName.lastIndexOf(".");

            if (extIndex != -1) {
                ext = baseName.substring(extIndex);
                extLength = ext.length();
                baseName = baseName.substring(0, extIndex);

                if (extLength == 1) {
                    ext = "";
                    extLength = 0;
                }
            }
        }

        List<String> result = new ArrayList<>(4);
        String language = locale.getLanguage();
        int languageLength = language.length();
        String country = locale.getCountry();
        int countryLength = country.length();
        String variant = locale.getVariant();
        int variantLength = variant.length();

        StringBuilder buffer = new StringBuilder(baseName);

        buffer.append(ext);
        result.add(buffer.toString());
        buffer.setLength(buffer.length() - extLength);

        if ((languageLength + countryLength + variantLength) == 0) {
            return result;
        }

        if (buffer.length() > 0) {
            buffer.append('_');
        }

        buffer.append(language);

        if (languageLength > 0) {
            buffer.append(ext);
            result.add(buffer.toString());
            buffer.setLength(buffer.length() - extLength);
        }

        if ((countryLength + variantLength) == 0) {
            return result;
        }

        // baseName_language_country
        buffer.append('_').append(country);

        if (countryLength > 0) {
            buffer.append(ext);
            result.add(buffer.toString());
            buffer.setLength(buffer.length() - extLength);
        }

        if (variantLength == 0) {
            return result;
        }

        // baseName_language_country_variant
        buffer.append('_').append(variant);

        buffer.append(ext);
        result.add(buffer.toString());
        buffer.setLength(buffer.length() - extLength);

        return result;
    }


}
