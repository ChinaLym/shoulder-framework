package org.shoulder.autoconfigure.core;

import org.shoulder.core.constant.ShoulderFramework;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import java.util.Properties;

/**
 * 设置默认配置项
 *
 * @author lym
 */
public class DefaultPropertiesStartingListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartingEvent event) {
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
        properties.put("shoulder.application.errorCodePrefix", "0");
        properties.put("shoulder.application.version", "v1");
        properties.put("shoulder.application.cluster", "false");
        properties.put("shoulder.application.dateFormat", "yyyy-MM-dd'T'HH:mm:ss.SSS Z");
        properties.put("shoulder.application.charset", "UTF-8");
        properties.put("shoulder.application.defaultLocale", "zh_CN");
        properties.put("shoulder.application.timeZone", "GMT+8:00");

        // 默认关闭 banner
        properties.put("mybatis-plus.global-config.banner=false", "false");

        return properties;
    }

}
