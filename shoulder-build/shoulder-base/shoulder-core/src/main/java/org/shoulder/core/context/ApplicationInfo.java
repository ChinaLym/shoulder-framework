package org.shoulder.core.context;

import lombok.extern.shoulder.SLog;
import org.apache.commons.codec.Charsets;
import org.shoulder.core.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 应用信息
 * 需要保证该类的赋值实际要足够早
 *
 * @author lym
 */
@SLog
public class ApplicationInfo {

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
     * 全局统一日期格式，默认世界标准时间格式
     */
    private static String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";

    /**
     * 全局统一字符集，默认 UTF-8
     */
    private static Charset charset = StandardCharsets.UTF_8;

    /**
     * 支持集群、多实例部署
     * 开启后一些缓存将使用外部缓存，默认使用 redis
     */
    private static boolean cluster = false;

    /**
     * 默认语言环境
     */
    private static Locale defaultLocale = Locale.CHINA;

    public static String appId() {
        return appId;
    }

    public static String version() {
        return version;
    }

    public static String errorCodePrefix() {
        return errorCodePrefix;
    }

    public static String dateFormat() {
        return dateFormat;
    }

    public static Charset charset() {
        return charset;
    }

    public static boolean cluster() {
        return cluster;
    }

    public static void initAppId(String appId) {
        ApplicationInfo.appId = appId;
    }

    public static void initErrorCodePrefix(String errorCodePrefix) {
        ApplicationInfo.errorCodePrefix = errorCodePrefix;
    }

    public static void initVersion(String version) {
        ApplicationInfo.version = version;
    }

    public static void initDateFormat(String dateFormat) {
        ApplicationInfo.dateFormat = dateFormat;
    }

    public static void initCharset(String charset) {
        ApplicationInfo.charset = StringUtils.isNotEmpty(charset) && Charset.isSupported(charset) ?
            Charset.forName(charset) : StandardCharsets.UTF_8;
    }

    public static void initCluster(boolean cluster) {
        ApplicationInfo.cluster = cluster;
    }

    /**
     * 获取语言标识
     *
     * @return 语言标识
     */
    public static Locale defaultLocale() {
        return defaultLocale;
    }

    /**
     * 设置语言标识
     *
     * @param  locale 语言标识
     */
    public static void initDefaultLocale(Locale locale) {
        ApplicationInfo.defaultLocale = locale;
    }

}
