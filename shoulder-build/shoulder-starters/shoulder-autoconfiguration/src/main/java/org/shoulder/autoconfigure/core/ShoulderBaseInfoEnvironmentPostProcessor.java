package org.shoulder.autoconfigure.core;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;
import java.util.TimeZone;

/**
 * 填充 shoulder 框架定义的基础信息
 *
 * @author lym
 */
@EnableConfigurationProperties(BaseAppProperties.class)
public class ShoulderBaseInfoEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String SHOULDER_PROPERTIES = "shoulderProperties";

    public ShoulderBaseInfoEnvironmentPostProcessor() {
        // just for debug
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        initApplicationInfo(environment);
    }

    @Override
    public int getOrder() {
        return -1000;
    }


    /**
     * 初始化应用信息
     * @see DefaultPropertiesStartingListener#defaultConfigurationProperties 会设置默认值
     * @param environment 环境与配置
     */
    private void initApplicationInfo(ConfigurableEnvironment environment) {
        Logger log = LoggerFactory.getLogger(getClass());

        Properties shoulderProperties = new Properties();

        String shoulderAppIdKey = "shoulder.application.id";
        String springAppNameKey = "spring.application.name";
        String appName = environment.getProperty(shoulderAppIdKey);
        if (StringUtils.isEmpty(appName)) {
            // appId 如果为空则采用 spring.application.name
            log.info(shoulderAppIdKey + " is empty, fallback to use " + springAppNameKey);
            appName = environment.getProperty(springAppNameKey);
            if (StringUtils.isEmpty(appName)) {
                appName = "unknown";
                // spring.application.name 或 shoulder.application.id 不能同时为空
                log.error("both of '" + shoulderAppIdKey + "' and '" + springAppNameKey +
                    "' are empty! set value please!");
            }
        }
        shoulderProperties.setProperty(shoulderAppIdKey, appName);
        AppInfo.initAppId(shoulderProperties.getProperty(shoulderAppIdKey));
        // 这些设置了默认值，不必担心为 null
        AppInfo.initErrorCodePrefix(environment.getProperty("shoulder.application.errorCodePrefix"));
        AppInfo.initVersion(environment.getProperty("shoulder.application.version"));
        AppInfo.initDateTimeFormat(environment.getProperty("shoulder.application.dateTimeFormat"));
        AppInfo.initCharset(environment.getProperty("shoulder.application.charset"));
        AppInfo.initCluster(Boolean.parseBoolean(environment.getProperty("shoulder.application.cluster")));
        AppInfo.initDefaultLocale(StringUtils.parseLocale(environment.getProperty("shoulder.application.defaultLocale")));
        AppInfo.initTimeZone(TimeZone.getTimeZone(environment.getProperty("shoulder.application.timeZone")));

        // -----
        // todo 【开发】security token 认证响应默认为 json

        if (!shoulderProperties.isEmpty()) {
            environment.getPropertySources().addFirst(new PropertiesPropertySource(SHOULDER_PROPERTIES, shoulderProperties));
        }

    }


}
