package org.shoulder.autoconfigure.core;

import jakarta.annotation.Nonnull;
import org.shoulder.core.constant.ShoulderFramework;
import org.shoulder.core.context.AppInfo;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

import java.util.Properties;

/**
 * 设置默认配置项
 * 此处动作发生在 Spring 生命周期较早阶段：加载了环境配置，但还未加载 application.properties；在此之后，Spring 还会做以下事情：
 * 1. 加载配置文件（application.properties 或 application.yml，并将配置解析为 PropertySource）
 * 2. 处理 Profile（激活 Profile 并加载 Profile 特定的配置）
 * 3. 解析  ${} 占位符、设置默认值。
 * 4. 准备 Environment，确保 Environment 包含完整的配置信息。
 * 5. 触发事件 ApplicationEnvironmentPreparedEvent，通知监听器 Environment 已准备就绪。
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
        properties.putIfAbsent("shoulder.application.cacheKeySplit", ":");

        // 默认关闭 banner
        properties.put("mybatis-plus.global-config.banner=false", "false");

        // todo P1【开发】security token 认证响应默认为 json

        return properties;
    }

}
