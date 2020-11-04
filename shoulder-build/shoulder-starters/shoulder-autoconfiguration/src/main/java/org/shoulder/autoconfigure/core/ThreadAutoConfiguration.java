package org.shoulder.autoconfigure.core;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.Threads;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * 线程相关的配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
public class ThreadAutoConfiguration implements ApplicationListener<ContextClosedEvent> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ExecutorService shoulderThreadPool;

    @PreDestroy
    @Bean(Threads.DEFAULT_THREAD_POOL_NAME)
    @ConditionalOnMissingBean(name = Threads.DEFAULT_THREAD_POOL_NAME)
    public ExecutorService shoulderThreadPool() {
        // 默认使用 5 个线程
        ExecutorService executorService = new ThreadPoolExecutor(5, 5,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(3000),
            r -> {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setDaemon(true);
                thread.setName("shoulder");
                return thread;
            });
        Threads.setExecutorService(executorService);
        shoulderThreadPool = executorService;
        return executorService;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent contextClosedEvent) {
        try {
            log.info("{} clean start...", getClass().getSimpleName());
            shoulderThreadPool.shutdown();
            log.info("{} clean finished.", getClass().getSimpleName());
        } catch (Exception e) {
            // on shutDown 钩子可能抛异常
            log.error(getClass().getSimpleName() + " clean FAIL! - ", e);
        }
    }
}
