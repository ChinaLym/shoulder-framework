package org.shoulder.core.concurrent.enhance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 线程增强器
 *
 * 是否扩展方法参数，拿到原来的 runnable ？
 *
 * @author lym
 */
public class ThreadEnhanceHelper {

    private static List<ThreadEnhancer> enhancers = new LinkedList<>();

    public static Runnable doEnhance(Runnable runnable) {
        EnhancedRunnable enhancedRunnable = new EnhancedRunnable(runnable);
        for (ThreadEnhancer enhancer : enhancers) {
            enhancedRunnable = enhancer.doEnhance(enhancedRunnable);
        }
        return enhancedRunnable;
    }

    public static <T> Callable<T> doEnhance(Callable<T> callable) {
        EnhancedCallable<T> enhancedCallable = new EnhancedCallable<>(callable);
        for (ThreadEnhancer enhancer : enhancers) {
            enhancedCallable = enhancer.doEnhance(enhancedCallable);
        }
        return enhancedCallable;
    }

    public static synchronized void register(ThreadEnhancer enhancer) {
        enhancers.add(enhancer);
    }

    public static synchronized void register(Collection<? extends ThreadEnhancer> enhancers) {
        ThreadEnhanceHelper.enhancers.addAll(enhancers);
    }

    public static void register(int order, ThreadEnhancer enhancer) {
        enhancers.add(order, enhancer);
    }

}
