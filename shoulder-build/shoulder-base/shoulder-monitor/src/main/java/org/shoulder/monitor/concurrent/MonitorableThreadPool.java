package org.shoulder.monitor.concurrent;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.*;

/**
 * 带指标可监控的线程池，推荐需要稳定执行、重要的业务使用，以更好的掌握系统运行状态
 *
 * @author lym
 * @see MonitorableRunnable 任务（Runnable）可以有标签（任务名/类名）
 */
public class MonitorableThreadPool extends ThreadPoolExecutor {

    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(MonitorableThreadPool.class);

    /**
     * 线程池名称，一般根据业务名称进行唯一命名，以便更好的管理线程池。
     * 如同步用户信息业务 syncUserInfo 批量导入用户 importUser
     */
    private final String poolName;

    /**
     * 执行计时器
     */
    private ThreadLocal<Long> workerStartTimeStamp = new ThreadLocal<>();

    /**
     * 当前参数，供监控访问，而非每次都访问线程池的属性
     */
    private ThreadPoolMetrics metrics;


    /**
     * 线程池满的拒绝策略
     */
    private static final RejectedExecutionHandler DEFAULT_HANDLER = new AbortPolicy();


    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param poolName        线程池名称
     */
    public MonitorableThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, String poolName) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(),
            DEFAULT_HANDLER, poolName);
    }

    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param poolName        线程池名称
     */
    public MonitorableThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, String poolName) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, DEFAULT_HANDLER, poolName);
    }

    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param poolName        线程池名称
     */
    public MonitorableThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler, String poolName) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler
            , poolName);
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
     * @param poolName        线程池名称
     */
    public MonitorableThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler, String poolName) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

        // 初始化指标
        this.poolName = poolName;
        initMetrics();
        setRejectedExecutionHandler(new MonitorableRejectHandler(handler, metrics));
    }

    private void initMetrics() {
        metrics = new ThreadPoolMetrics(poolName);
        this.metrics.corePoolSize().set(getCorePoolSize());
        this.metrics.activeCount().set(getActiveCount());
        this.metrics.maximumPoolSize().set(getMaximumPoolSize());
        this.metrics.largestPoolSize().set(0);
        this.metrics.queueCapacity().set(getQueue().remainingCapacity());
        //this.getKeepAliveTime(TimeUnit.MILLISECONDS)

    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);

        //metrics.corePoolSize().set(getCorePoolSize());
        //metrics.maximumPoolSize().set(getMaximumPoolSize());

        metrics.activeCount().set(getActiveCount());
        metrics.poolSize().set(getPoolSize());
        metrics.largestPoolSize().set(getLargestPoolSize());

        metrics.queueSize().set(getQueue().size());

        workerStartTimeStamp.set(System.currentTimeMillis());

    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        long finishStamp = System.currentTimeMillis();
        long consuming = finishStamp - workerStartTimeStamp.get();
        workerStartTimeStamp.remove();
        // 默认使用 ms 记录执行时间
        this.metrics.taskExecuteTime(r).record(consuming, TimeUnit.MILLISECONDS);

        super.afterExecute(r, t);

        if (t == null) {
            // 正常执行完毕
        } else {
            // 异常执行完毕
            metrics.exceptionCount(r).increment();
        }

        // 自身线程还没释放，去掉 active
        this.metrics.activeCount().set(getActiveCount() - 1);
        // 可通过完成数 + 队列数计算，不精确，但可以避免锁竞争
        this.metrics.taskCount().set(getTaskCount());
        // 完成任务 + 自身，因为自身线程还没释放
        this.metrics.completedTaskCount().set(getCompletedTaskCount() + 1);
        // 看看队列里还有多少
        this.metrics.queueSize().set(getQueue().size());
    }

    /**
     * 立即关闭时
     *
     * @return 没有执行的任务
     */
    @Nonnull
    @Override
    public List<Runnable> shutdownNow() {
        // 统计已执行任务、正在执行任务、未执行任务数量
        log.info("{} Going to immediately shutdown. Executed tasks: {}, Running tasks: {}, Pending tasks: {}",
            this.poolName, this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        metrics.completedTaskCount().set(getCompletedTaskCount());
        metrics.activeCount().set(getActiveCount());
        metrics.queueSize().set(getQueue().size());
        return super.shutdownNow();
    }

    /**
     * 线程池延迟关闭时（不再接收、执行新任务，等待线程池里的任务都执行完毕）
     */
    @Override
    public void shutdown() {
        // 统计已执行任务、正在执行任务、未执行任务数量
        log.info("{} Going to shutdown. Executed tasks: {}, Running tasks: {}, Pending tasks: {}",
            this.poolName, this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        metrics.completedTaskCount().set(getCompletedTaskCount());
        metrics.activeCount().set(getActiveCount());
        metrics.queueSize().set(getQueue().size());
        super.shutdown();
    }


    /**
     * 修改核心线程数
     *
     * @param newCorePoolSize [0 , maximumPoolSize]
     */
    @Override
    public void setCorePoolSize(int newCorePoolSize) {
        super.setCorePoolSize(newCorePoolSize);
        this.metrics.corePoolSize().set(newCorePoolSize);
    }


    /**
     * 修改最大线程数
     *
     * @param newMaximumPoolSize corePoolSize 小于等于 maximumPoolSize 且 maximumPoolSize 大于 0
     */
    @Override
    public void setMaximumPoolSize(int newMaximumPoolSize) {
        super.setMaximumPoolSize(newMaximumPoolSize);
        this.metrics.maximumPoolSize().set(newMaximumPoolSize);
    }



    /* 注意，若希望获取某一时刻的状态，必须加锁获取。仅依次获取可能不准（因为不同的get不在同一时刻执行）。
    getActiveCount()	        中正在执行任务的线程数量
    getTaskCount()	            任务总数（已经执行 + 未执行）
    getCompletedTaskCount()	    已完成的任务数量，该值小于等于 taskCount

    getCorePoolSize()	        的核心线程数量
    getMaximumPoolSize()	    的最大线程数量
    getPoolSize()	            当前的线程数量
    getLargestPoolSize()	    线程池存在以来最大线程数量。通过该值可以判断是否满过（达到maximumPoolSize）
    */

    /**
     * 获取线程池名称
     *
     * @return 线程池名称
     */
    public String getPoolName() {
        return poolName;
    }

}
