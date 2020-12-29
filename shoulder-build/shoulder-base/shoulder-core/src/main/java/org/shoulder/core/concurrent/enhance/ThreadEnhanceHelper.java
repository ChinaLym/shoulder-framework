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
        Runnable result = runnable;
        for (ThreadEnhancer enhancer : enhancers) {
            result = enhancer.doEnhance(result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> Callable<T> doEnhance(Callable<T> callable) {
        Callable<T> result = callable;
        for (ThreadEnhancer enhancer : enhancers) {
            result = enhancer.doEnhance(result);
        }
        return result;
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
