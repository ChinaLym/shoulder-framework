package org.shoulder.autoconfigure.core.config;

import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.shoulder.core.context.ApplicationInfo;
import org.shoulder.core.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * 填充 shoulder 框架定义的基础信息
 * @author lym
 */
@Configuration
@EnableConfigurationProperties(BaseAppProperties.class)
public class ShoulderBaseInfoAutoConfiguration implements EnvironmentPostProcessor, Ordered {

    private static final String SHOULDER_PROPERTIES = "shoulderProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String appIdKey = "shoulder.application.id";
        if(StringUtils.isEmpty(environment.getProperty(appIdKey))){
            // appId 如果为空则采用 spring.application.name
            Properties properties = new Properties();
            properties.setProperty(appIdKey, environment.getProperty("spring.application.name"));
            environment.getPropertySources().addFirst(new PropertiesPropertySource(SHOULDER_PROPERTIES, properties));
        }
        initApplicationInfo(environment);
    }

    @Override
    public int getOrder() {
        return -1000;
    }


    /**
     * 初始化应用信息
     * @param environment 环境与配置
     */
    private void initApplicationInfo(ConfigurableEnvironment environment){
        ApplicationInfo.initAppId(environment.getProperty("shoulder.application.id"));
        ApplicationInfo.initErrorCodePrefix(environment.getProperty("shoulder.application.errorCodePrefix"));
        ApplicationInfo.initVersion(environment.getProperty("shoulder.application.version"));
        ApplicationInfo.initDateFormat(environment.getProperty("shoulder.application.dateFormat"));
        ApplicationInfo.initCharset(environment.getProperty("shoulder.application.charset"));
        ApplicationInfo.initCluster(Boolean.parseBoolean(environment.getProperty("shoulder.application.cluster")));
        ApplicationInfo.initDefaultLocale(StringUtils.parseLocale(environment.getProperty("shoulder.application.defaultLocale")));
    }


}
