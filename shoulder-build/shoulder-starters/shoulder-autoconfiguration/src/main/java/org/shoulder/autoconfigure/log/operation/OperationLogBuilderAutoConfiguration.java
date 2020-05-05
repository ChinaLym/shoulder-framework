package org.shoulder.autoconfigure.log.operation;

import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.util.OperationLogBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Bean 装配：操作日志 - 创建
 * 该类主要做了一件事情：OperationLogBuilder.init。
 * 直接使用该类装配需要保证框架将下面三个@Value值塞进了spring中
 *
 * @author lym
 */
@Configuration
@ConditionalOnClass(OperationLog.class)
public class OperationLogBuilderAutoConfiguration {

    /**
     * 应用名称作为服务表标识
     */
    @Value("${spring.application.name:}")
    private String appName;

    /** 激活日志创建器 */
    @PostConstruct
    public void postConstruct() {
        OperationLogBuilder.init(appName);
    }

}
