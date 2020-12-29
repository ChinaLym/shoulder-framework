package org.shoulder.core.concurrent.enhance;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 线程增强器
 *
 * @author lym
 */
public class ThreadEnhanceHelper {

    private static List<ThreadEnhancer> enhancers = new LinkedList<>();

    public static Runnable doEnhance(Runnable runnable) {
        Runnable result = runnable;
        for (ThreadEnhancer enhancer : enhancers) {
            result = enhancer.doEnhance(runnable);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> Callable<T> doEnhance(Callable<T> callable) {
        Callable result = callable;
        for (ThreadEnhancer enhancer : enhancers) {
            result = enhancer.doEnhance(callable);
        }
        return (Callable<T>) result;
    }

    public static void register(ThreadEnhancer enhancer) {
        enhancers.add(enhancer);
    }

    public static void register(int order, ThreadEnhancer enhancer) {
        enhancers.add(order, enhancer);
    }

}
