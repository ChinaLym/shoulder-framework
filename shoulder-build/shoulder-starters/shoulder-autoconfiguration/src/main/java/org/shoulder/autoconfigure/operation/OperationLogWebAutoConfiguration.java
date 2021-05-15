package org.shoulder.autoconfigure.operation;

import org.shoulder.log.operation.model.OperationLogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 操作日志-当前用户信息
 *
 * @author lym
 */
@Configuration
@ConditionalOnClass(OperationLogDTO.class)
@AutoConfigureAfter(OperationLogAspect.class)
@ConditionalOnWebApplication
@EnableConfigurationProperties(OperationLogProperties.class)
@ConditionalOnProperty(value = "shoulder.log.operation.enable", havingValue = "true", matchIfMissing = true)
public class OperationLogWebAutoConfiguration implements WebMvcConfigurer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private OperationLogProperties operationLogProperties;

    /**
     * 用户信息填充器，不能为空
     */
    @Lazy
    @Autowired
    private OperationLogOperatorInfoInterceptor operationLogOperatorInfoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (operationLogOperatorInfoInterceptor != null) {
            registry.addInterceptor(operationLogOperatorInfoInterceptor)
                .order(operationLogProperties.getInterceptorOrder());
        } else {
            log.warn("no found any OperationLogOperatorInfoInterceptor, " +
                "will always use application.name as default operator.");
        }
        WebMvcConfigurer.super.addInterceptors(registry);
    }

    /**
     * web 上下文中对用户信息的 AOP 解析
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "shoulder.log.operation.defaultOperatorInterceptor", havingValue = "enable",
        matchIfMissing = true)
    public OperationLogOperatorInfoInterceptor operationLogOperatorInfoInterceptor() {
        log.info("use default operatorResolver: doNothing");
        return new CurrentContextOperatorInfoInterceptor();
    }
}
