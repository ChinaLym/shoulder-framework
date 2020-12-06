package com.example.demo4;

import org.shoulder.autoconfigure.monitor.thread.MonitorableThreadPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 可监控的线程池
 *
 * @author lym
 */
@SpringBootApplication
public class Demo4Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo4Application.class, args);
    }

    /**
     * 打开 http://localhost:8080/actuator/prometheus，可以看到 demo4_thread_pool 的指标
     */
    @Bean
    public MonitorableThreadPool monitorableThreadPool() {
        return new MonitorableThreadPool(5, 10, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(200), "test");
    }


}
