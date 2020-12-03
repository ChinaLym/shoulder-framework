package org.shoulder.core.concurrent;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * @author lym
 */
public class NotifyOnFinishCallable<V> implements Callable<V> {

    private Callable<V> delegate;

    /**
     * 入参为执行结果、出参为需要返回的结果
     * 框架可以通过该钩子对 Callable 进行增强，如触发执行结束通知，业务相关逻辑不推荐使用该方式
     */
    private Function<V, V> function;

    public NotifyOnFinishCallable(Callable<V> delegate, Function<V, V> function) {
        this.delegate = delegate;
        this.function = function;
    }

    @Override
    public V call() throws Exception {
        V result = null;
        try {
            result = delegate.call();
        } finally {
            result = function.apply(result);
        }
        return result;
    }
}
