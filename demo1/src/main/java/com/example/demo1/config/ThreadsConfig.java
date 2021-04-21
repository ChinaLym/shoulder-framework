package com.example.demo1.config;

import com.example.demo1.controller.concurrent.ThreadEnhancerDemoController;
import org.shoulder.core.concurrent.enhance.EnhancedRunnable;
import org.shoulder.core.concurrent.enhance.ThreadEnhancer;
import org.shoulder.monitor.concurrent.MonitorableThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author lym
 */
@Configuration
public class ThreadsConfig {

    /**
     * 可监控的线程池
     * 打开 http://localhost:8080/actuator/prometheus，可以看到自动添加了 demo1_thread_pool_threads 的指标
     */
    @Bean
    public MonitorableThreadPool monitorableThreadPool() {
        // 测试一下 new 一个 MonitorableThreadPool
        return new MonitorableThreadPool(5, 10, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(200), "test");
    }

    @Bean
    public ThreadEnhancer myThreadEnhancer() {
        return new ThreadEnhancer() {
            @Override
            public EnhancedRunnable doEnhance(EnhancedRunnable runnable) {
                if (runnable.isInstanceOf(ThreadEnhancerDemoController.SomeBusinessOperation.class)) {
                    return new EnhancedRunnable(() -> {
                        try {
                            System.out.println("我要在所有线程池执行之前干点事情");
                            runnable.run();
                            System.out.println("我要在所有线程池正常执行之后干点事情");
                        } catch (Throwable t) {
                            System.out.println("我要在所有线程池执行遇到异常干点事情");
                            throw t;
                        } finally {
                            System.out.println("我要在所有线程执行之后干点事情");
                        }
                    });
                }
                return runnable;
            }
        };
    }

}
