package org.shoulder.autoconfigure.config;

import org.shoulder.core.context.BaseContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 填充 shoulder 框架定义的基础信息
 * @author lym
 */
public class ShoulderBaseInfoAutoConfiguration implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String applicationName = environment.getProperty("spring.application.name");
        BaseContextHolder.setServiceId(applicationName);
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
