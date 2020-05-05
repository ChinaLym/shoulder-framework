package org.shoulder.autoconfigure.log.operation.async;

import org.shoulder.log.operation.async.executors.OpLogExecutor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;

import java.util.concurrent.Executor;

/**
 * 允许使用者自定义 带有日志装饰的异步线程池 的自动装配
 * @author lym
 */
public class OpLogAsyncCustomizer extends AsyncConfigurerSupport {
    private final AsyncConfigurer delegate;

    public OpLogAsyncCustomizer(AsyncConfigurer delegate) {
        this.delegate = delegate;
    }

    @Override
    public Executor getAsyncExecutor() {
        return this.delegate.getAsyncExecutor() instanceof OpLogExecutor ?
                this.delegate.getAsyncExecutor() :
                new OpLogExecutor(this.delegate.getAsyncExecutor());
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return this.delegate.getAsyncUncaughtExceptionHandler();
    }
}
