package org.shoulder.autoconfigure.operation;

import org.shoulder.log.operation.dto.OperationLogDTO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
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
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OperationLogDTO.class)
@AutoConfigureAfter(OperationLogAspect.class)
@ConditionalOnNotWebApplication
public class OperationLogWebAutoConfiguration implements WebMvcConfigurer {

    /**
     * 用户信息填充器，不能为空
     */
    @Lazy
    @Autowired
    private OperationLogOperatorInfoInterceptor operationLogOperatorInfoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (operationLogOperatorInfoInterceptor != null) {
            registry.addInterceptor(operationLogOperatorInfoInterceptor).order(-10000);
        } else {
            LoggerFactory.getLogger(getClass()).warn("OperationLogOperatorInfoInterceptor can't be null!");
        }
        WebMvcConfigurer.super.addInterceptors(registry);
    }

    /**
     * web 上下文中对用户信息的 AOP 解析
     */
    @Bean
    @ConditionalOnMissingBean
    public OperationLogOperatorInfoInterceptor operationLogSsoOperatorInfoInterceptor() {
        return new CurrentContextOperatorInfoInterceptor();
    }
}
