package org.shoulder.core.constant;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * 框架的定义
 *
 * @author <a href="mailto:cnlym@foxmail.com">lym</a>
 */
public class ShoulderFramework {

    public static final long SERIAL_ID = 666L;

    /**
     * 框架名称
     */
    public static final String NAME = "shoulder";

    /**
     * 当前框架版本
     */
    public static String VERSION;

    /**
     * 框架错误码-模块前缀
     */
    public static String SHOULDER_ERROR_CODE_MODULE_PREFIX = "";

    static {
        try (InputStream in = ShoulderFramework.class.getClassLoader().getResourceAsStream("shoulder-version.txt")) {
            Properties props = new Properties();
            Objects.requireNonNull(in, "inputStream parameter is null -- shoulder-version.txt read fail!");
            props.load(in);
            String val = props.getProperty("version");
            final String versionPlaceholder = "${project.version}";
            if (val != null && !versionPlaceholder.equals(val)) {
                VERSION = val;
            }
            val = props.getProperty("errorCodePrefix");
            SHOULDER_ERROR_CODE_MODULE_PREFIX = val;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
