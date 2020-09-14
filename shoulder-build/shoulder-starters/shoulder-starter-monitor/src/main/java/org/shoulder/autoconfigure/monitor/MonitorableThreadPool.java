package org.shoulder.autoconfigure.monitor;

import lombok.Data;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

/**
 * 带指标可监控的线程池，推荐需要稳定执行、重要的业务使用，以更好的掌握系统运行状态
 * todo JDK 中实现的当且仅当任务队列满了时才会创建新线程，但如果这时候突发来多个任务，则导致任务很可能被拒绝
 * 实际中应根据队列容量自动扩容线程数，如，当队列中任务数达到上限的 70%、80%、90%，则自动扩容线程，而不是满了之后才扩容
 * 动态设置参数实现： 对接配置中心
 * 监控、告警实现： 对接 prometheus，过载告警
 * 操作记录与审计： 对接日志中心，变更通知
 * https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html
 * todo 任务可以有标签（任务名/类名）；拆为参数可动态调整；可监控两个类
 *
 * @author lym
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
     * 慢任务执行阈值，超过这个时间，则需要记录并发出告警。
     * 默认为 30s，可动态调整
     */
    private long slowTaskThreshold = Duration.ofSeconds(30).toMillis();

    /**
     * 当前参数，供监控访问，而非每次都访问线程池的属性
     */
    private ThreadPoolMetrics metrics;

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
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.poolName = poolName;
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
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.poolName = poolName;
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
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        this.poolName = poolName;
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
        this.poolName = poolName;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        workerStartTimeStamp.set(System.currentTimeMillis());
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        long finishStamp = System.currentTimeMillis();
        long consuming = workerStartTimeStamp.get() - finishStamp;
        workerStartTimeStamp.remove();

        super.afterExecute(r, t);

        boolean noException = t != null;

        if (noException) {
            // 正常执行完毕

        } else {
            // 异常执行完毕
        }

        //this.metrics.taskCount().set(getTaskCount());
    }

    /**
     * 立即关闭时
     *
     * @return 没有执行的任务
     */
    @NonNull
    @Override
    public List<Runnable> shutdownNow() {
        // 统计已执行任务、正在执行任务、未执行任务数量
        log.info("{} Going to immediately shutdown. Executed tasks: {}, Running tasks: {}, Pending tasks: {}",
            this.poolName, this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
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
        super.shutdown();
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

    public long getSlowTaskThreshold() {
        return slowTaskThreshold;
    }

    public void setSlowTaskThreshold(long slowTaskThreshold) {
        this.slowTaskThreshold = slowTaskThreshold;
    }

    @Data
    public static class ThreadPoolInfoDTO {
        /**
         * 线程池名称（标识） 不可修改
         */
        private String poolName;
        /**
         * 核心线程大小
         */
        private Integer corePoolSize;
        /**
         * 最大线程大小
         */
        private Integer maxPoolSize;
        /**
         * 当前线程个数
         */
        private Integer currentThreadNum;
        /**
         * 当前活跃线程个数
         */
        private Integer currentActiveCount;
        /**
         * 历史最大线程大小
         */
        private Integer largestPoolSize;

        /**
         * 队列类型
         */
        private String queueType;
        /**
         * 队列最大大小
         */
        private Integer queueCapacity;
        /**
         * 队列中任务数量
         */
        private Integer queueSize;
        // 剩余容量
        //private Integer queueRemainingCapacity;

        private Integer rejectCount;

        /**
         * 任务执行耗时，根据此值，统计最大、平均、90% 95% 99%  fixme Map
         */
        private Integer executeTime;
    }

}
