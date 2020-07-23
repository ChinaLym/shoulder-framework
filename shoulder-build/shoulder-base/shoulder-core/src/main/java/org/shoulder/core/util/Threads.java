package org.shoulder.core.util;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import org.shoulder.core.delay.DelayTaskHolder;
import org.shoulder.core.delay.DelayTask;

import java.util.concurrent.*;

/**
 * 线程工具类
 * 提供创建线程和延时任务方法的封装
 * 注意：
 * 1. 封装的方法，会自动继承线程变量
 * 2. 该类必须在 ApplicationContextAware 之后使用，否则 IllegalStateException。
 * <p>
 * 如果单纯使用以及更多底层 api，推荐直接用 @Autowired 注入线程池使用
 *
 * @author lym
 */
public class Threads {

    private final static ThreadPoolExecutor DEFAULT_THREAD_POOL;
    public final static String DEFAULT_THREAD_POOL_NAME = "shoulderThreadPool";

    static {
        DEFAULT_THREAD_POOL = SpringUtils.getBean(DEFAULT_THREAD_POOL_NAME);
    }

    /**
     * 放入线程池执行，且传递父线程的 threadLocal 变量到子线程（默认为浅拷贝）
     * 若希望为深拷贝，重写 {@link com.alibaba.ttl.TransmittableThreadLocal#copy} 方法即可
     *
     * @param runnable 要执行的任务
     */
    public static void execute(Runnable runnable) {
        // 线程变量自动继承和清理
        Runnable ttlRunnable = getTtlRunnable(runnable);
        DEFAULT_THREAD_POOL.execute(ttlRunnable);
    }

    /**
     * 放入线程池执行，且传递父线程的 threadLocal 变量到子线程（默认为浅拷贝）
     * 若希望为深拷贝，重写 {@link com.alibaba.ttl.TransmittableThreadLocal#copy} 方法即可
     *
     * @param callable 要执行的任务
     * @return 当前任务执行的结果
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        return DEFAULT_THREAD_POOL.submit(getTtlRunnable(callable));
    }

    /**
     * 使用该方法包装线程类，将自动将线程放入延迟队列并延时执行
     *
     * @param runnable    要延时执行的事情
     * @param time 延时时间
     * @param unit time 的单位
     */
    public static void delay(Runnable runnable, long time, TimeUnit unit) {
        DelayTask task = new DelayTask(runnable, time, unit);
        delay(task);
    }

    /**
     * 使用该方法包装线程类，将自动将线程放入延迟队列并延时执行
     *
     * @param delayTask 要延时执行的任务
     */
    public static void delay(DelayTask delayTask) {
        DelayTaskHolder.put(delayTask);
    }

    /**
     * 添加了空校验的 {@link TtlRunnable#get}
     */
    public static Runnable getTtlRunnable(Runnable r){
        if (r == null) {
            throw new NullPointerException("runnable can not be null!");
        }
        return TtlRunnable.get(r, true, false);
    }

    /**
     * 添加了空校验的 {@link TtlCallable#get}
     */
    public static <T>  Callable<T>  getTtlRunnable(Callable<T> c){
        if (c == null) {
            throw new NullPointerException("callable can not be null!");
        }
        return TtlCallable.get(c, true, false);
    }

}
