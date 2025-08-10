package org.shoulder.autoconfigure.core;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Objects;
import java.util.Properties;
import java.util.TimeZone;

/**
 * 填充 shoulder 框架定义的基础信息
 * <p>
 * 注意：不支持不配置 application.yml 而是手动将配置设置在 @PropertySource；这种 hack 场景！
 * <p>原因：这部分配置是在解析bean时才读取的，读取时机太晚了，有的bean已经设置完成了，Spring 不支持
 * 若希望支持这种场景在 autoconfiguration 阶段使用 AppInfo.xx 则需要手动读一遍所有配置文件, 几乎不可能（PropertiesLoaderUtils#loadProperties
 * Spring 启动流程：
 * 1. Application.prepareContext 这个过程没加载用户配置
 * *** ApplicationStartingEvent、
 * *** ApplicationEnvironmentPreparedEvent、
 * *** ApplicationContextInitializedEvent、
 * *** BootstrapContextClosedEvent、
 * *** ApplicationPreparedEvent、
 * invokeBeanFactoryPostProcessors
 * 2. Application.prepareContext 中 invokeBeanFactoryPostProcessors 加载用户配置
 * *** invokeBeanDefinitionRegistryPostProcessors: ConfigurationClassPostProcessor 加载bean 定义时加载了所选profile所有的配置
 * *** DefaultListableBeanFactory 加载
 * -- configuration 配置（SpringApplication#refreshContex、上下文刷新-初始化非lzay Bean 时
 * -- Spring 读取用户 application.yml 等（ConfigurationPropertiesBindingPostProcessor）
 * ServletWebServerInitializedEvent
 * ContextRefreshedEvent
 * 3. 启动（Application.afterRefresh）
 * ApplicationStartedEvent
 * AvailabilityChangeEvent(CORRECT)
 * ApplicationReadyEvent
 * AvailabilityChangeEvent（ACCEPTING_TRAFFIC）
 *
 * @author lym
 * @see SimpleApplicationEventMulticaster#multicastEvent(ApplicationEvent)
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
     *
     * @param environment 环境与配置
     * @see DefaultPropertiesStartingListener#defaultConfigurationProperties 会设置默认值
     */
    private void initApplicationInfo(ConfigurableEnvironment environment) {
        Logger log = ShoulderLoggers.SHOULDER_CONFIG;

        Properties shoulderProperties = new Properties();

        String shoulderAppIdKey = "shoulder.application.id";
        String springAppNameKey = "spring.application.name";
        String appName = environment.getProperty(shoulderAppIdKey);
        if (StringUtils.isEmpty(appName)) {
            // appId 如果为空则采用 spring.application.name
            log.info(shoulderAppIdKey + " is empty, fallback to use " + springAppNameKey);
            appName = environment.getProperty(springAppNameKey);
            if (StringUtils.isEmpty(appName)) {
                // 有可能是不配置 application.yml 而是手动将配置设置在 @PropertySource；这种 hack 场景！不支持！
                // todo 可以读取 propertySource 检查是否这种情况
                // spring.application.name 或 shoulder.application.id 不能同时为空
                BaseRuntimeException e = new BaseRuntimeException(CommonErrorCodeEnum.CODING,
                        "both of '" + shoulderAppIdKey + "' and '" + springAppNameKey +
                                "' are empty! please set value or check if you mistaken, tip: Can not set configurations" +
                                " into @PropertySource(file) instead of spring stand application.xml!");
                throw e;
            }
        }
        shoulderProperties.setProperty(shoulderAppIdKey, appName);
        AppInfo.initAppId(shoulderProperties.getProperty(shoulderAppIdKey));
        // 这些设置了默认值，不必担心为 null
        AppInfo.initErrorCodePrefix(Objects.requireNonNull(environment.getProperty("shoulder.application.errorCodePrefix")));
        AppInfo.initVersion(Objects.requireNonNull(environment.getProperty("shoulder.application.version")));
        AppInfo.initDateTimeFormat(Objects.requireNonNull(environment.getProperty(BaseAppProperties.dateTimeFormatConfigPath)));
        AppInfo.initCharset(Objects.requireNonNull(environment.getProperty("shoulder.application.charset")));
        AppInfo.initCluster(Boolean.parseBoolean(environment.getProperty("shoulder.application.cluster")));
        AppInfo.initDefaultLocale(StringUtils.parseLocale(environment.getProperty("shoulder.application.defaultLocale")));
        AppInfo.initCacheKeySplit(environment.getProperty("shoulder.application.cacheKeySplit"));
        AppInfo.initTimeZone(TimeZone.getTimeZone(environment.getProperty("shoulder.application.timeZone")));

        // -----

        if (!shoulderProperties.isEmpty()) {
            environment.getPropertySources().addFirst(new PropertiesPropertySource(SHOULDER_PROPERTIES, shoulderProperties));
        }

    }


}
