package com.example.demo1.config;

import com.example.demo1.controller.concurrent.ThreadEnhancerDemoController;
import org.shoulder.core.concurrent.enhance.ThreadEnhancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lym
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadEnhancer myThreadEnhancer() {
        return new ThreadEnhancer() {
            @Override
            public Runnable doEnhance(Runnable runnable) {
                if (runnable instanceof ThreadEnhancerDemoController.SomeBusinessOperation) {
                    return () -> {
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
                    };
                }
                return runnable;
            }
        };
    }

}
