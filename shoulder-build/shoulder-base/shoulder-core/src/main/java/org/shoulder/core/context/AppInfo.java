package org.shoulder.core.context;

import jakarta.annotation.Nonnull;
import org.shoulder.core.guid.InstanceIdProvider;
import org.shoulder.core.i18.LocaleInfo;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.core.util.StringUtils;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 应用信息
 * 需要保证该类的赋值实际要足够早，shoulder 会在应用启动时初始化该类配置
 *
 * @author lym
 */
public class AppInfo {

    private static final Logger log = LoggerFactory.getLogger(AppInfo.class);

    /**
     * T can be replaced by any character
     * Z +8
     * ZZ +08
     * ZZZ +08:00
     */
    public static final String UTC_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";

    /**
     * 应用标识 / 服务名称 https://bbs.huaweicloud.com/blogs/115110
     */
    private static String appId = "";

    /**
     * 应用标识 / 服务名称 https://bbs.huaweicloud.com/blogs/115110
     */
    private static long instanceId = -1;

    //private static String runningMode = "JAR"; ip/domain

    /**
     * 应用错误码前缀
     */
    private static String errorCodePrefix = "0x0000";

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
    private static String dateTimeFormat = UTC_DATE_TIME_FORMAT;

    /**
     * 全局统一字符集，默认 UTF-8
     */
    private static Charset charset = LocaleInfo.getSystemDefault().getCharset();

    /**
     * 默认语言环境，从用户中获取不到地区/语言信息时，将采用该值，若不设置则从系统中获取
     */
    private static Locale defaultLocale = LocaleInfo.getSystemDefault().getLocale();

    /**
     * 时区，若不设置则从系统中获取
     */
    private static TimeZone timeZone = TimeZone.getDefault();

    public static String appId() {
        return appId;
    }

    public static long instanceId() {
        if (instanceId == -1) {
            instanceId = ContextUtils.getBean(InstanceIdProvider.class).getCurrentInstanceId();
        }
        return instanceId;
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

    public static String dateTimeFormat() {
        return dateTimeFormat;
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

    public static void initAppId(@Nonnull String appId) {
        Assert.notNull(appId, "appId can't be null");
        AppInfo.appId = appId;
        log.info("initAppId: " + appId);
    }

    public static void initErrorCodePrefix(@Nonnull String errorCodePrefix) {
        Assert.notNull(errorCodePrefix, "errorCodePrefix can't be null");
        // 建议以 0x 开头表示 16 进制，暂时强制要求，有需要后再取消
        Assert.isTrue(StringUtils.startsWith(errorCodePrefix, "0x"), "errorCodePrefix need startWith '0x'");
        AppInfo.errorCodePrefix = errorCodePrefix;
        log.info("initErrorCodePrefix: " + errorCodePrefix);
    }

    public static void initVersion(@Nonnull String version) {
        Assert.notNull(version, "version can't be null");
        AppInfo.version = version;
        log.info("initVersion: " + version);
    }

    public static void initCluster(boolean cluster) {
        AppInfo.cluster = cluster;
        log.info("initCluster: " + cluster);
    }


    public static void initDateTimeFormat(@Nonnull String dateFormat) {
        Assert.notNull(dateFormat, "dateFormat can't be null");
        AppInfo.dateTimeFormat = dateFormat;
        log.info("initDateFormat: " + dateFormat);
    }

    public static void initCharset(@Nonnull String charset) {
        Assert.notNull(charset, "charset can't be null");
        AppInfo.charset = StringUtils.isNotEmpty(charset) && Charset.isSupported(charset) ?
                Charset.forName(charset) : LocaleInfo.getSystemDefault().getCharset();
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

    /**
     * 运行方式 todo 【增强】运行模式 jar / war
     */
    public enum RunMode {
        /**
         * spring-boot jar 包
         */
        JAR,
        /**
         * 打成 war 包放到容器中跑
         */
        WAR,
        /**
         * docker 镜像
         */
        DOCKER,
        /*
         * 使用了边车模式的框架
         */
        //@Deprecated
        //DOCKER_SIDE_CAR,
        ;
    }

}
