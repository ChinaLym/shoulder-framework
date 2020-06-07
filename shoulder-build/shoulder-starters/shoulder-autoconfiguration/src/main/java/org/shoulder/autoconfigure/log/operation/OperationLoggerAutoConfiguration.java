package org.shoulder.autoconfigure.log.operation;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.async.OpLogRunnable;
import org.shoulder.log.operation.format.DefaultOperationLogFormatter;
import org.shoulder.log.operation.format.OperationLogFormatter;
import org.shoulder.log.operation.intercept.OperationLoggerInterceptor;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.impl.AsyncOperationLogger;
import org.shoulder.log.operation.logger.impl.DefaultOperationLogger;
import org.shoulder.log.operation.util.OpLogContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.Executors;

/**
 * This configuration class registers a {@link OperationLogger} able to logging operation-log.
 *
 * @author lym
 */
@Configuration
@ConditionalOnClass(OperationLog.class)
@EnableConfigurationProperties(OperationLogProperties.class)
public class OperationLoggerAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(OperationLoggerAutoConfiguration.class);

    private final OperationLogProperties operationLogProperties;

    public OperationLoggerAutoConfiguration(OperationLogProperties operationLogProperties) {
        this.operationLogProperties = operationLogProperties;
    }

    /**
     * Provided a singleThread executor {@link Executors#newSingleThreadExecutor} for default.
     * {@link AsyncOperationLogger} entrust {@link DefaultOperationLogger} as a delegator with log.
     *
     * @return {@link AsyncOperationLogger}
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnProperty(value = "shoulder.log.operation.logger.async", havingValue = "true", matchIfMissing = true)
    public AsyncOperationLogger asyncOperationLogger(OperationLogFormatter operationLogFormatter) {
        int threadNum = operationLogProperties.getThreadNum();
        String threadName = operationLogProperties.getThreadName();

        log.info("OperationLogger-async=true,threadNum=" + threadNum + ",threadName=" + threadName);
        return new AsyncOperationLogger()
                .setExecutor(Executors.newFixedThreadPool(threadNum,
                        r -> {
                            Thread loggingThread = new Thread(
                                    new OpLogRunnable(r), threadName
                            );
                            loggingThread.setDaemon(true);
                            return loggingThread;
                        }
                ))
                .setLogger(new DefaultOperationLogger(operationLogFormatter));
    }


    /**
     * Provide a sync logger for default.
     *
     * @see DefaultOperationLogger
     */
    @Bean
    @ConditionalOnMissingBean(value = {OperationLogger.class})
    public OperationLogger operationLogger(OperationLogFormatter operationLogFormatter) {
        log.info("OperationLogger-async=false");
        return new DefaultOperationLogger(operationLogFormatter);
    }

    /**
     * Provide a logger for default.
     *
     * @see DefaultOperationLogFormatter
     */
    @Bean
    @ConditionalOnMissingBean
    public OperationLogFormatter defaultOperationLogFormatter() {
        return new DefaultOperationLogFormatter();
    }

    /**
     * 1. support cross thread log.
     * 2. support opLog interceptor.
     */
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        OperationLogger operationLogger =
                applicationContext.getBean(OperationLogger.class);

        // set cross thread logger
        OpLogContextHolder.setOperationLogger(operationLogger);

        // register all logInterceptors.
        Collection<OperationLoggerInterceptor> loggerInterceptors =
                event.getApplicationContext().getBeansOfType(OperationLoggerInterceptor.class).values();
        if (CollectionUtils.isNotEmpty(loggerInterceptors)) {
            loggerInterceptors.forEach(operationLogger::addInterceptor);
        }

        if (log.isDebugEnabled()) {
            StringJoiner sj = new StringJoiner(",", "loggerInterceptors=[", "]");
            if (CollectionUtils.isNotEmpty(loggerInterceptors)) {
                loggerInterceptors.stream().map(i -> i.getClass().getName()).forEach(sj::add);
            }
            log.debug(sj.toString());
        }

    }
}
