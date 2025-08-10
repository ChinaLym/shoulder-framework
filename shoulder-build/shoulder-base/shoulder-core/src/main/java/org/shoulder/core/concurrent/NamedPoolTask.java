package org.shoulder.core.concurrent;

import java.util.concurrent.ExecutorService;

/**
 * 提交到线程池执行的任务
 *
 * @author lym
 */
public interface NamedPoolTask {

    /**
     * 任务名称，打印日志使用
     */
    String getTaskName();

    /**
     * 执行内容
     */
    void process();

    /**
     * 获取期望使用的线程池 BeanName
     *
     * @return 期望使用的线程池 BeanName
     */
    default String getExecutorServiceBeanName() {
        return Threads.SHOULDER_THREAD_POOL_NAME;
    }

    /**
     * 获取期望使用的线程池
     *
     * @return 期望使用的线程池
     */
    default ExecutorService getExecutorService() {
        return Threads.resolveExecutorServiceFromContext(getExecutorServiceBeanName());
    }
}
