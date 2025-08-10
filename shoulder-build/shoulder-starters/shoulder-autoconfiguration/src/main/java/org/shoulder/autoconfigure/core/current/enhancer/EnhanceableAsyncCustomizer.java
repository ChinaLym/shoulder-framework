package org.shoulder.autoconfigure.core.current.enhancer;

import org.shoulder.core.concurrent.enhance.EnhanceableExecutor;
import org.shoulder.core.concurrent.enhance.EnhanceableExecutorMark;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;

import java.util.concurrent.Executor;

/**
 * 允许使用者自定义 AsyncConfigurer 时，框架能力仍然生效
 *
 * @author lym
 */
public class EnhanceableAsyncCustomizer extends AsyncConfigurerSupport {

    private final AsyncConfigurer delegate;

    public EnhanceableAsyncCustomizer(AsyncConfigurer delegate) {
        this.delegate = delegate;
    }

    @Override
    public Executor getAsyncExecutor() {
        return this.delegate.getAsyncExecutor() instanceof EnhanceableExecutorMark ?
            this.delegate.getAsyncExecutor() :
                EnhanceableExecutor.wrap(this.delegate.getAsyncExecutor());
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return this.delegate.getAsyncUncaughtExceptionHandler();
    }
}
