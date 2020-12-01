package org.shoulder.autoconfigure.operation;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.log.operation.async.OpLogRunnable;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.format.OperationLogFormatter;
import org.shoulder.log.operation.format.impl.ShoulderOpLogFormatter;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.impl.AsyncOperationLogger;
import org.shoulder.log.operation.logger.impl.Sl4jOperationLogger;
import org.shoulder.log.operation.logger.intercept.OperationLoggerInterceptor;
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

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.*;

/**
 * This configuration class registers a {@link OperationLogger} able to logging operation-log.
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OperationLogDTO.class)
@EnableConfigurationProperties(OperationLogProperties.class)
public class OperationLoggerAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(OperationLoggerAutoConfiguration.class);

    private final OperationLogProperties operationLogProperties;

    public OperationLoggerAutoConfiguration(OperationLogProperties operationLogProperties) {
        this.operationLogProperties = operationLogProperties;
    }

    /**
     * Provided a singleThread executor {@link Executors#newSingleThreadExecutor} for default.
     * {@link AsyncOperationLogger} entrust {@link ShoulderOpLogFormatter} as a delegator with log.
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
        // default rejectExecutionHandler is throw Ex, use ignore if opLog is not important.
        ExecutorService opLogExecutorService = new ThreadPoolExecutor(1, 1,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(3000),
            r -> {
                Thread loggingThread = new Thread(new OpLogRunnable(r), threadName);
                // drop the opLog when application shutDown
                loggingThread.setDaemon(true);
                return loggingThread;
            });

        return new AsyncOperationLogger()
            .setExecutorService(opLogExecutorService)
            .setLogger(new Sl4jOperationLogger(operationLogFormatter));
    }


    /**
     * Provide a sync logger for default.
     *
     * @see Sl4jOperationLogger
     */
    @Bean
    @ConditionalOnMissingBean(value = {OperationLogger.class})
    public OperationLogger operationLogger(OperationLogFormatter operationLogFormatter) {
        log.info("OperationLogger-async=false");
        return new Sl4jOperationLogger(operationLogFormatter);
    }

    /**
     * Provide a logger for default.
     *
     * @see ShoulderOpLogFormatter
     */
    @Bean
    @ConditionalOnMissingBean
    public OperationLogFormatter defaultOperationLogFormatter() {
        return new ShoulderOpLogFormatter();
    }

    /**
     * 1. support cross thread log.
     * 2. support opLog interceptor.
     */
    @Override
    public void onApplicationEvent(@Nonnull ContextRefreshedEvent event) {
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
