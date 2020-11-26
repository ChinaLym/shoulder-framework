package org.shoulder.autoconfigure.core;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
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
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BaseAppProperties.class)
public class ShoulderBaseInfoAutoConfiguration implements EnvironmentPostProcessor, Ordered {

    private static final String SHOULDER_PROPERTIES = "shoulderProperties";

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
     *
     * @param environment 环境与配置
     */
    private void initApplicationInfo(ConfigurableEnvironment environment) {
        Logger log = LoggerFactory.getLogger(getClass());

        Properties shoulderProperties = new Properties();

        String appIdKey = "shoulder.application.id";
        if (StringUtils.isEmpty(environment.getProperty(appIdKey))) {
            // appId 如果为空则采用 spring.application.name
            String springAppNameKey = "spring.application.name";
            log.debug(appIdKey + " is empty, fallback to use " + springAppNameKey);

            String appName = environment.getProperty(springAppNameKey);
            if (StringUtils.isNotEmpty(appName)) {
                shoulderProperties.setProperty(appIdKey, appName);
            } else {
                // spring.application.name 或 shoulder.application.id 不能同时为空
                log.error("both of '" + appIdKey + "' and '" + springAppNameKey +
                    "' are empty! set value please!");
            }
        }
        AppInfo.initAppId(shoulderProperties.getProperty(appIdKey));
        AppInfo.initErrorCodePrefix(environment.getProperty("shoulder.application.errorCodePrefix"));
        AppInfo.initVersion(environment.getProperty("shoulder.application.version"));
        AppInfo.initDateFormat(environment.getProperty("shoulder.application.dateFormat"));
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
