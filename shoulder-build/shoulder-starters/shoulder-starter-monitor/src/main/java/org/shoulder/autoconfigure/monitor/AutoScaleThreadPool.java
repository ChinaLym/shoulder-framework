package org.shoulder.autoconfigure.monitor;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

/**
 * 自动设置核心线程数的线程池，以更合理的利用线程资源
 *
 * JDK 中实现的当且仅当任务队列满了时才会创建新线程，但如果这时候突发来多个任务，则导致任务很可能被拒绝
 * 实际中应根据队列容量自动扩容线程数，如，当队列中任务数达到上限的 70%、80%、90%，则自动扩容线程，而不是满了之后才扩容
 *
 * 执行前，若队列中任务数 > 75% 队列容量 且 threadSize < maxSize，将线程数加一，默认冷却时间 5s
 * 执行后，若队列中任务数 < 25% 队列容量 且 threadSize < originCoreSize，将 core 线程数减少一，默认冷却时间 5s
 *  
 * 动态设置参数实现： 对接配置中心
 * 监控、告警实现： 对接 prometheus，过载告警
 * 操作记录与审计： 对接日志中心，变更通知
 * https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html
 *
 * @author lym
 */
public class AutoScaleThreadPool extends ThreadPoolExecutor {

    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(AutoScaleThreadPool.class);

    /**
     * 最小扩容时间间隔
     */
    private Duration expansionDuration = Duration.ofSeconds(5);

    /**
     * 最小缩容时间间隔
     */
    private Duration shrinkageDuration = Duration.ofSeconds(10);


    /**
     * 触发扩容的 负载因子
     */
    private float expansionLoadFactor = 0.75f;

    /**
     * 触发缩容的 负载因子
     */
    private float shrinkageLoadFactor = 0.25f;


    // -------------- todo init ---------------

    /**
     * 触发扩容时的线程数
     */
    private final int expansionOnTaskCount = 0;

    /**
     * 触发缩容的线程数
     */
    private final int shrinkageOnTaskCount = 0;

    /**
     * 队列最大容量
     */
    private final int queueCapacity = 100;

    /**
     * 下次扩容最早时间点
     */
    private volatile Instant expansionInstant = Instant.EPOCH;

    /**
     * 下次缩容最早时间点
     */
    private volatile Instant shrinkageInstant = Instant.EPOCH;

    /**
     * 每次扩容增加的线程数
     */
    private final int expansionThreadOneTime = 1;

    /**
     * 每次缩容增加的线程数
     */
    private final int shrinkageThreadOneTime = 1;

    /**
     * 负责扩容的线程，避免并发问题
     */
    //private VarHandle varHandle = VarHandle.


    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     */
    public AutoScaleThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     */
    public AutoScaleThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     */
    public AutoScaleThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param threadFactory   线程工厂
     */
    public AutoScaleThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);

        // todo cas 避免并发问题
        if(Instant.now().isAfter(expansionInstant)) {
            synchronized (this){
                // 检测上次修改时间是否变了，版本号，因为最小修改时间间隔可能是0？
                if(Instant.now().isAfter(expansionInstant)) {

                }
                // 默认冷却时间 5s
                int queueTaskNum = getQueue().size();
                int currentThreadNum;
                if(queueTaskNum > expansionOnTaskCount && (currentThreadNum = getPoolSize()) < getMaximumPoolSize()){
                    setCorePoolSize(currentThreadNum + 1);
                }
                // todo 执行前，若队列中任务数 > 75% 队列容量 且 threadSize < maxSize，将线程数加一，
            }

        }

    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        // todo 执行后，若队列中任务数 < 25% 队列容量 且 threadSize < originCoreSize，将 core 线程数减少一，默认冷却时间 5s

    }

}
