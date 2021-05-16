package org.shoulder.autoconfigure.operation;

import cn.hutool.http.useragent.UserAgentUtil;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.OperationLoggerInterceptor;
import org.shoulder.log.operation.logger.intercept.UserAgentParserInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UserAgent 解析
 *
 * @author lym
 */
@ConditionalOnClass({UserAgentUtil.class, OperationLogger.class})
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "shoulder.log.operation.enable", havingValue = "true", matchIfMissing = true)
public class OperationLogUserAgentResolverAutoConfiguration {

    @Bean
    @ConditionalOnProperty(value = "shoulder.log.operation.resolveUserAgent", havingValue = "true", matchIfMissing = true)
    public OperationLoggerInterceptor opLogUserAgentParserInterceptor() {
        return new UserAgentParserInterceptor();
    }
}
