package org.shoulder.core.delay;

import lombok.extern.shoulder.SLog;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.Threads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 延时任务搬运工，负责从 {@link DelayTaskHolder} 中拿任务，放入线程池中。
 * 该线程在容器初始化完毕后将在线程池中开始进行延迟任务的搬运工作，且长期占用线程池一个线程。
 *
 * @author lym
 */
@SLog
@Service
public class DelayTaskPorter implements Runnable, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    @Qualifier(Threads.DEFAULT_THREAD_POOL_NAME)
    private ThreadPoolExecutor defaultThreadPool;

    private static volatile boolean running = false;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            start();
        }
    }

    /**
     * 启动延迟任务调度线程（需要容器启动后执行）
     * 补充：ApplicationContextAware的注入时机：bean后处理器其中有一个叫 ApplicationContextAwareProcessor,其中的处理方法调用setApplicationContext方法
     * <p>
     * {@link AbstractApplicationContext#prepareBeanFactory}
     * {@link AbstractApplicationContext#refresh}
     */
    private synchronized void start() {
        if(running){
            return;
        }
        running = true;
        defaultThreadPool.execute(this);
        log.info("DelayTaskPorter Initialized.");
    }

    @Override
    public void run() {
        log.info("DelayTaskPorter started.");
        Thread.currentThread().setDaemon(true);
        Thread.currentThread().setName("delayTaskPorter");
        DelayTask delay;
        while (true) {
            try {
                // 从延时队列中获取任务
                delay = DelayTaskHolder.next();
                Runnable task = delay.getTask();
                if (task == null) {
                    continue;
                }
                defaultThreadPool.execute(task);

            } catch (Exception e) {
                log.error("delay queue execute failed", e);
            }
        }
    }

}
