package org.shoulder.autoconfigure.core.config;

import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.shoulder.core.context.ApplicationInfo;
import org.shoulder.core.context.BaseContextHolder;
import org.shoulder.core.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 填充 shoulder 框架定义的基础信息
 * @author lym
 */
@Configuration
@EnableConfigurationProperties(BaseAppProperties.class)
public class ShoulderBaseInfoAutoConfiguration implements EnvironmentPostProcessor, Ordered {

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
     * @param environment 环境与配置
     */
    private void initApplicationInfo(ConfigurableEnvironment environment){
        String appId = environment.getProperty("shoulder.application.id");
        if(StringUtils.isEmpty(appId)){
            // todo 放置于 shoulder.application.id 中
            appId = environment.getProperty("spring.application.name");
        }
        ApplicationInfo.initAppId(appId);
        ApplicationInfo.initErrorCodePrefix(environment.getProperty("shoulder.application.errorCodePrefix"));
        ApplicationInfo.initVersion(environment.getProperty("shoulder.application.version"));
        ApplicationInfo.initDateFormat(environment.getProperty("shoulder.application.dateFormat"));
        ApplicationInfo.initCharset(environment.getProperty("shoulder.application.charset"));
        ApplicationInfo.initCluster(Boolean.valueOf(environment.getProperty("shoulder.application.cluster")));
    }


}
