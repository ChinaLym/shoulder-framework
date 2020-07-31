package org.shoulder.core.delay;

import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延迟任务 DTO，包装 runnable
 *
 * @author lym
 */
public class DelayTask implements Delayed {

    /**
     * 延迟执行的纳秒数
     */
    private final long time;

    /**
     * 要执行的任务
     */
    private final Runnable task;

    /**
     * 运行该任务的线程池名称，如果设置该值将从 spring 上下文中寻找名为 threadPoolName 的线程池，未找到或不设置则使用默认的
     */
    private final String threadPoolName;

    /**
     * @param task           要执行的任务
     * @param nanoTimeOut    多久后执行，单位纳秒
     * @param threadPoolName 线程池 bean 名称
     */
    private DelayTask(Runnable task, long nanoTimeOut, String threadPoolName) {
        this.time = System.nanoTime() + nanoTimeOut;
        this.task = task;
        this.threadPoolName = threadPoolName;
    }

    /**
     * @param task 要执行的任务
     * @param time 多久后执行
     * @param unit time的时间单位
     */
    public DelayTask(Runnable task, long time, TimeUnit unit) {
        this(task, TimeUnit.NANOSECONDS.convert(time, unit), (String) null);
    }

    public DelayTask(Runnable task, long time, TimeUnit unit, String threadPoolName) {
        this(task, TimeUnit.NANOSECONDS.convert(time, unit), threadPoolName);
    }

    /**
     * @param task      要执行的任务
     * @param daleyTime 多久后执行
     */
    public DelayTask(Runnable task, Duration daleyTime) {
        this(task, daleyTime.toNanos(), (String) null);
    }

    /**
     * @param task           要执行的任务
     * @param daleyTime      多久后执行
     * @param threadPoolName 线程池 bean 名称
     */
    public DelayTask(Runnable task, Duration daleyTime, String threadPoolName) {
        this(task, daleyTime.toNanos(), threadPoolName);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.time - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    @Override
    public int compareTo(@NonNull Delayed delayed) {
        DelayTask other = (DelayTask) delayed;
        long diff = time - other.time;
        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    public Runnable getTask() {
        return this.task;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((task == null) ? 0 : task.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        DelayTask other = (DelayTask) o;
        return this.hashCode() == other.hashCode();
    }

}
