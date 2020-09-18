package com.example.demo3.config;

import org.shoulder.autoconfigure.monitor.thread.MonitorableThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author lym
 */
@Configuration
public class DemoThreadPoolConfig {

    /**
     * 打开 http://localhost:8080/actuator/prometheus，搜 demo4_thread_pool 可以看到其指标
     *
     * @return 可监控的线程池
     */
    @Bean
    public MonitorableThreadPool monitorableThreadPool() {
        return new MonitorableThreadPool(5, 10, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(200), "test");
    }

}
