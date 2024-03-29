package org.shoulder.autoconfigure.core;

import jakarta.annotation.Nonnull;
import org.shoulder.core.constant.ShoulderFramework;
import org.shoulder.core.context.AppInfo;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

import java.util.Properties;

/**
 * 设置默认配置项
 *
 * @author lym
 */
public class DefaultPropertiesStartingListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(@Nonnull ApplicationStartingEvent event) {
        // 注意：当应用中存在spring-cloud-context包时，这个事件将触发2次
        Properties properties = defaultConfigurationProperties();
        // 配置属性优先级：System > application.properties > SpringApplication
        event.getSpringApplication().setDefaultProperties(properties);
    }

    /**
     * 返回应用配置项的默认值
     *
     * @return 应用配置项的默认值
     */
    private Properties defaultConfigurationProperties() {
        Properties properties = new Properties();
        // =================== shoulder 框架信息 ========================
        properties.put("shoulder.version", ShoulderFramework.VERSION);

        // =================== 应用信息默认值 ========================
        // 默认不使用前缀
        properties.putIfAbsent("shoulder.application.errorCodePrefix", "0x0000");
        properties.putIfAbsent("shoulder.application.version", "v1");
        properties.putIfAbsent("shoulder.application.cluster", "false");
        properties.putIfAbsent(BaseAppProperties.dateTimeFormatConfigPath, AppInfo.UTC_DATE_TIME_FORMAT);
        properties.putIfAbsent("shoulder.application.charset", "UTF-8");
        properties.putIfAbsent("shoulder.application.defaultLocale", "zh_CN");
        properties.putIfAbsent("shoulder.application.timeZone", "GMT+8:00");

        // 默认关闭 banner
        properties.put("mybatis-plus.global-config.banner=false", "false");

        return properties;
    }

}
