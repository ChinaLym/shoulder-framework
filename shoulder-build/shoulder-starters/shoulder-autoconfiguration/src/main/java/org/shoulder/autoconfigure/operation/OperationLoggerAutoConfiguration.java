package org.shoulder.autoconfigure.operation;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.format.OperationLogFormatter;
import org.shoulder.log.operation.format.impl.ShoulderOpLogFormatter;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.OperationLoggerInterceptor;
import org.shoulder.log.operation.logger.impl.AsyncOperationLogger;
import org.shoulder.log.operation.logger.impl.BufferedOperationLogger;
import org.shoulder.log.operation.logger.impl.JdbcOperationLogger;
import org.shoulder.log.operation.logger.impl.LogOperationLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
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

    @Bean
    @Order(0)
    @ConditionalOnProperty(value = "shoulder.log.operation.logger.async", havingValue = "true", matchIfMissing = true)
    public BeanPostProcessor asyncLoggerBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@Nonnull Object bean, String beanName) throws BeansException {
                if (!(bean instanceof OperationLogger)) {
                    return bean;
                }
                int threadNum = operationLogProperties.getLogger().getThreadNum();
                String threadName = operationLogProperties.getLogger().getThreadName();
                log.debug("OperationLogger-async=true,threadNum=" + threadNum + ",threadName=" + threadName);
                // default rejectExecutionHandler is throw Ex, use ignore if opLog is not important.
                CustomizableThreadFactory opLogThreadFactory = new CustomizableThreadFactory(threadName);
                // 可以设置为 true，因为操作日志一般并不是非记录不可
                // opLogThreadFactory.setDaemon(true);
                ExecutorService opLogExecutorService = new ThreadPoolExecutor(threadNum, threadNum,
                    60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), opLogThreadFactory);

                return new AsyncOperationLogger()
                    .setExecutorService(opLogExecutorService)
                    .setLogger((OperationLogger) bean);
            }
        };
    }

    @Bean
    @Order(1)
    @ConditionalOnProperty(value = "shoulder.log.operation.logger.buffered", havingValue = "true")
    public BeanPostProcessor bufferedLoggerBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@Nonnull Object bean, String beanName) throws BeansException {
                if (!(bean instanceof OperationLogger)) {
                    return bean;
                }
                String threadName = operationLogProperties.getLogger().getThreadName();
                ScheduledExecutorService scheduledExecutorService =
                    Executors.newScheduledThreadPool(1, new CustomizableThreadFactory(threadName));

                long flushInterval = operationLogProperties.getLogger().getFlushInterval().toMillis();
                int flushThreshold = operationLogProperties.getLogger().getFlushThreshold();
                int perFlushMax = operationLogProperties.getLogger().getPerFlushMax();
                return new BufferedOperationLogger(new ConcurrentLinkedQueue<>(), (OperationLogger) bean, scheduledExecutorService,
                    flushInterval, flushThreshold, perFlushMax);
            }
        };
    }

    /**
     * Provide a slf4j logger for default.
     *
     * @see LogOperationLogger
     */
    @Bean
    @ConditionalOnMissingBean(value = {OperationLogger.class})
    @ConditionalOnProperty(name = "shoulder.log.operation.logger.type", havingValue = "logger", matchIfMissing = true)
    public LogOperationLogger logOperationLogger(OperationLogFormatter operationLogFormatter) {
        return new LogOperationLogger(operationLogFormatter);
    }

    /**
     * Provide a jdbc logger for default.
     *
     * @see JdbcOperationLogger
     */
    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(name = "shoulder.log.operation.logger.type", havingValue = "jdbc")
    @ConditionalOnMissingBean(value = {OperationLogger.class})
    public JdbcOperationLogger jdbcOperationLogger(DataSource dataSource) {
        return new JdbcOperationLogger(dataSource);
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
