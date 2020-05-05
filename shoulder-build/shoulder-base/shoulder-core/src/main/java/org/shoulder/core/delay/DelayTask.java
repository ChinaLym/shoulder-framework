package org.shoulder.core.delay;

import org.shoulder.core.util.Threads;
import org.springframework.lang.NonNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延迟任务 DTO，包装 runnable
 *
 * @author lym
 */
public class DelayTask implements Delayed {

    private final long time;

    private final Runnable task;

    private final String threadPoolName;

    /**
     * @param task        要执行的任务
     * @param nanoTimeOut 多久后执行，单位纳秒
     */
    public DelayTask(Runnable task, long nanoTimeOut) {
        this(task, nanoTimeOut, Threads.DEFAULT_THREAD_POOL_NAME);
    }

    public DelayTask(Runnable task, long nanoTimeOut, String threadPoolName) {
        this.time = System.nanoTime() + nanoTimeOut;
        this.task = Threads.getTtlRunnable(task);
        this.threadPoolName = threadPoolName;
    }

    /**
     * @param task 要执行的任务
     * @param time 多久后执行
     * @param unit time的计时单位
     */
    public DelayTask(Runnable task, long time, TimeUnit unit) {
        this(task, TimeUnit.NANOSECONDS.convert(time, unit));
    }

    public DelayTask(Runnable task, long time, TimeUnit unit, String threadPoolName) {
        this(task, TimeUnit.NANOSECONDS.convert(time, unit), threadPoolName);
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
