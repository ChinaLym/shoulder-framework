package org.shoulder.core.concurrent;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.StringUtils;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 普通异步任务
 *
 * @author lym
 */
public interface AsyncTask extends NamedPoolTask {


    /**
     * 期待的完成时间，如果在这个时间未完成则需要中断
     *
     * @return 最晚完成时间点
     */
    default Instant exceptedFinishTime() {
        return null;
    }

    /**
     * 处理异常
     *
     * @param task 任务运行信息
     * @param e    异常
     */
    default void handleException(Threads.TaskInfo task, Exception e) {
        ShoulderLoggers.SHOULDER_THREADS.error("{} execute exception. submitTime={}, runStartTime={}", task.taskName(), task.taskSubmitTime(), task.runStartTimeRef().get(), e);
    }

    /**
     * 处理超时
     *
     * @param task 任务运行信息
     */
    default void handleTimeout(Threads.TaskInfo task) {
        ShoulderLoggers.SHOULDER_THREADS.error("{} timeout. submitTime={}, runStartTime={}", task.taskName(), task.taskSubmitTime(), task.runStartTimeRef().get());
    }

    /**
     * 处理任务被取消
     *
     * @param task 任务运行信息
     */
    default void handleCancelled(Threads.TaskInfo task) {
        ShoulderLoggers.SHOULDER_THREADS.info("{} execute canceled.", task.taskName());
    }

    static AsyncTask create(@Nonnull String taskName, @Nonnull Runnable runnable) {
        return create(taskName, runnable, (String) null);
    }

    static AsyncTask create(@Nonnull String taskName, @Nonnull Runnable runnable, @Nonnull String executorServiceBeanName) {
        return create(taskName, runnable, null, null, null, null, executorServiceBeanName, null);
    }

    static AsyncTask create(@Nonnull String taskName, @Nonnull Runnable runnable, @Nonnull ExecutorService executorService) {
        return create(taskName, runnable, null, null, null, null, null, executorService);
    }

    static AsyncTask create(@Nonnull String taskName, @Nonnull Runnable runnable, @Nonnull Consumer<Threads.TaskInfo> exceptionCallBack, @Nullable Instant exceptedFinishTime, ExecutorService executorService) {
        return create(taskName, runnable, (t, e) -> exceptionCallBack.accept(t), exceptionCallBack, exceptionCallBack, exceptedFinishTime, null, executorService);
    }

    static AsyncTask create(@Nonnull String taskName, @Nonnull Runnable runnable,
                            @Nullable BiConsumer<Threads.TaskInfo, Exception> exceptionCallBack,
                            @Nullable Consumer<Threads.TaskInfo> timeoutCallBack,
                            @Nullable Consumer<Threads.TaskInfo> cancelCallback,
                            @Nullable Instant exceptedFinishTime, @Nullable String executorServiceBeanName, @Nullable ExecutorService executorService) {
        return new AsyncTask() {
            @Override
            public String getTaskName() {
                return taskName;
            }

            @Override
            public void process() {
                runnable.run();
            }

            @Override
            public void handleException(Threads.TaskInfo task, Exception e) {
                if (exceptionCallBack != null) {
                    exceptionCallBack.accept(task, e);
                } else {
                    AsyncTask.super.handleException(task, e);
                }
            }

            @Override
            public void handleTimeout(Threads.TaskInfo task) {
                if (timeoutCallBack != null) {
                    timeoutCallBack.accept(task);
                } else {
                    AsyncTask.super.handleTimeout(task);
                }
            }

            @Override
            public void handleCancelled(Threads.TaskInfo task) {
                if (cancelCallback != null) {
                    cancelCallback.accept(task);
                } else {
                    AsyncTask.super.handleCancelled(task);
                }
            }

            @Override
            public Instant exceptedFinishTime() {
                return exceptedFinishTime;
            }

            @Override
            public String getExecutorServiceBeanName() {
                return StringUtils.isEmpty(executorServiceBeanName) ? AsyncTask.super.getExecutorServiceBeanName() : executorServiceBeanName;
            }

            @Override
            public ExecutorService getExecutorService() {
                return executorService == null ? AsyncTask.super.getExecutorService() : executorService;
            }
        };
    }
}
