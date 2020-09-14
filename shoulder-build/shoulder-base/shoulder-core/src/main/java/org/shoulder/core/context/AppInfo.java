package org.shoulder.core.context;

import lombok.extern.shoulder.SLog;
import org.shoulder.core.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 应用信息
 * 需要保证该类的赋值实际要足够早，shoulder 会在应用启动时初始化该类配置
 *
 * @author lym
 */
@SLog
public class AppInfo {

    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";

    /**
     * 应用标识
     */
    private static String appId = "";

    /**
     * 应用错误码前缀
     */
    private static String errorCodePrefix = "";

    /**
     * 应用版本（用于灰度发布、版本兼容等）
     */
    private static String version = "v1";

    /**
     * 是否支持集群、多实例部署，默认不支持
     * 开启后一些缓存将使用外部缓存，如 redis
     */
    private static boolean cluster = false;

    /**
     * 全局统一日期格式，默认世界标准时间格式
     */
    private static String dateFormat = UTC_DATE_FORMAT;

    /**
     * 全局统一字符集，默认 UTF-8
     */
    private static Charset charset = StandardCharsets.UTF_8;

    /**
     * 默认语言环境，从用户中获取不到地区/语言信息时，将采用该值，若不设置则从系统中获取
     */
    private static Locale defaultLocale = Locale.getDefault();

    /**
     * 时区，若不设置则从系统中获取
     */
    private static TimeZone timeZone = TimeZone.getDefault();

    public static String appId() {
        return appId;
    }

    public static String errorCodePrefix() {
        return errorCodePrefix;
    }

    public static String version() {
        return version;
    }

    public static boolean cluster() {
        return cluster;
    }

    public static String dateFormat() {
        return dateFormat;
    }

    public static Charset charset() {
        return charset;
    }

    /**
     * 获取默认地区/语言标识
     *
     * @return 地区/语言标识
     */
    public static Locale defaultLocale() {
        return defaultLocale;
    }

    public static TimeZone timeZone() {
        return timeZone;
    }

    public static void initAppId(String appId) {
        AppInfo.appId = appId;
        log.info("initAppId: " + appId);
    }

    public static void initErrorCodePrefix(String errorCodePrefix) {
        AppInfo.errorCodePrefix = errorCodePrefix;
        log.info("initErrorCodePrefix: " + errorCodePrefix);
    }

    public static void initVersion(String version) {
        AppInfo.version = version;
        log.info("initVersion: " + version);
    }

    public static void initCluster(boolean cluster) {
        AppInfo.cluster = cluster;
        log.info("initCluster: " + cluster);
    }


    public static void initDateFormat(String dateFormat) {
        AppInfo.dateFormat = dateFormat;
        log.info("initDateFormat: " + dateFormat);
    }

    public static void initCharset(String charset) {
        AppInfo.charset = StringUtils.isNotEmpty(charset) && Charset.isSupported(charset) ?
            Charset.forName(charset) : StandardCharsets.UTF_8;
        log.info("initCharset: " + charset);
    }

    /**
     * 设置语言标识
     *
     * @param locale 语言标识
     */
    public static void initDefaultLocale(Locale locale) {
        AppInfo.defaultLocale = locale;
        log.info("initDefaultLocale: " + locale);
    }


    public static void initTimeZone(TimeZone timezone) {
        AppInfo.timeZone = timezone;
        log.info("initTimeZone: " + timezone);
    }
}
