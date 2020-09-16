package org.shoulder.autoconfigure.monitor.thread;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

/**
 * 可自动扩容、缩容核心线程数的线程池，（可更合理的利用线程资源、应对突发事件处理）适合平时节能模式处理，突然紧急情况下提前加速处理的场景
 *
 * JDK 中实现的当且仅当任务队列满了时才会创建新线程，但如果这时候突发来多个任务，则导致任务很可能被拒绝
 * 实际中应根据队列容量自动扩容线程数，如，当队列中任务数达到上限的 70%、80%、90%，则自动扩容线程，而不是满了之后才扩容，缓解大压力场景下触发
 *
 * 默认参数举例：
 * - 执行前，若队列中任务数 > 75% 队列容量 且 threadSize < maxSize，将线程数加一，默认冷却时间 5s
 * - 执行后，若队列中任务数 < 25% 队列容量 且 threadSize < originCoreSize，将 core 线程数减少一，默认冷却时间 5s
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
     * 是否支持扩容缩容，corePoolSize < maximumPoolSize
     */
    private final boolean supportScale;

    /**
     * 扩容触发条件：队列中的任务数
     */
    private final int expansionOnTaskCount;

    /**
     * 缩容触发条件：队列中的任务数
     */
    private final int shrinkageOnTaskCount;

    /**
     * 最小扩容时间间隔，需要 > 0
     */
    private final Duration expansionDuration;

    /**
     * 最小缩容时间间隔，需要  > 0
     */
    private final Duration shrinkageDuration;

    /**
     * 每次扩容增加的线程数
     */
    private final int expansionThreadOneTime;

    /**
     * 每次缩容增加的线程数
     */
    private final int shrinkageThreadOneTime;

    /**
     * 下次扩容最早时间点
     */
    private volatile Instant expansionInstant = Instant.EPOCH;

    /**
     * 下次缩容最早时间点
     */
    private volatile Instant shrinkageInstant = Instant.EPOCH;


    /**
     * 负责扩容的线程，避免并发问题
     */
    //private VarHandle varHandle = VarHandle.

    private static final RejectedExecutionHandler DEFAULT_HANDLER = new ThreadPoolExecutor.AbortPolicy();
    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     */
    public AutoScaleThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, ScaleRule scaleRule) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
            Executors.defaultThreadFactory(), DEFAULT_HANDLER, scaleRule);
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
    public AutoScaleThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, ScaleRule scaleRule) {

        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, DEFAULT_HANDLER, scaleRule);
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
    public AutoScaleThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler, ScaleRule scaleRule) {

        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
            Executors.defaultThreadFactory(), handler, scaleRule);
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
    public AutoScaleThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                               RejectedExecutionHandler handler, ScaleRule scaleRule) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        scaleRule.adjust(corePoolSize, maximumPoolSize, workQueue.remainingCapacity());
        this.supportScale = scaleRule.isSupportScale();
        this.expansionOnTaskCount = scaleRule.getExpansionOnTaskCount();
        this.expansionDuration = scaleRule.getExpansionDuration();
        this.expansionThreadOneTime = scaleRule.getExpansionThreadOneTime();
        this.shrinkageOnTaskCount = scaleRule.getShrinkageOnTaskCount();
        this.shrinkageDuration = scaleRule.getShrinkageDuration();
        this.shrinkageThreadOneTime = scaleRule.getShrinkageThreadOneTime();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);

        final Instant oldInstant = expansionInstant;
        final Instant now;
        if(supportScale && (now = Instant.now()).isAfter(oldInstant)) {
            // 必然存在并发问题，DCL
            synchronized (this){
                // 检测上次修改时间是否变了，版本号，因为最小修改时间间隔可能是0？
                if(oldInstant != expansionInstant){
                    return;
                }
                // 默认冷却时间 5s
                int queueTaskNum = getQueue().size();
                int currentThreadNum;
                // 若队列中任务数 > 队列扩容阈值（默认75%） 且未达到最大线程数 threadSize < maxSize，将线程数加一，
                if(queueTaskNum > expansionOnTaskCount && (currentThreadNum = getPoolSize()) < getMaximumPoolSize()){
                    setCorePoolSize(currentThreadNum + expansionThreadOneTime);
                }
                expansionInstant = now.plus(expansionDuration);
            }
        }

    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        final Instant oldInstant = shrinkageInstant;
        final Instant now;
        // 缩容冷却时间
        if(supportScale && (now = Instant.now()).isAfter(oldInstant)) {
            // 必然存在并发问题，DCL
            synchronized (this){
                // 检测上次修改时间是否变了，版本号，所以最小修改时间间隔>0？
                if(oldInstant != shrinkageInstant){
                    return;
                }
                // 默认冷却时间 5s
                int queueTaskNum = getQueue().size();
                int currentThreadNum;
                // 若队列中任务数 > 队列扩容阈值（默认75%） 且未达到最大线程数 threadSize < maxSize，将线程数加一，
                if(queueTaskNum < shrinkageOnTaskCount && (currentThreadNum = getPoolSize()) > getCorePoolSize()){
                    setCorePoolSize(currentThreadNum - shrinkageThreadOneTime);
                }
                shrinkageInstant = now.plus(shrinkageDuration);
            }
        }
    }

    /**
     * 自动扩容、缩容规则
     * 该类职责完全服务于外部类，故置为内部类
     *
     * @author lym
     */
    public static final class ScaleRule {

        // =========== 0 - 1 之间，这两者间距越大，越不会发生扩容后又要缩容，但若越接近极限值，则队列的意义就越小 ==============

        /**
         * 触发扩容的 负载因子
         */
        private float expansionLoadFactor = 0.75f;
        /**
         * 触发缩容的 负载因子
         */
        private float shrinkageLoadFactor = 0.25f;

        // ============= 触发间隔，过小可能频繁触发扩容，过大则失去了对抗突发情况的韧性 ================
        /**
         * 最小扩容时间间隔，需要 > 0
         */
        private Duration expansionDuration = Duration.ofSeconds(5);

        /**
         * 最小缩容时间间隔，需要  > 0
         */
        private Duration shrinkageDuration = Duration.ofSeconds(10);

        // ============= 每次扩大缩小核心线程数时的个数，默认为1 ================
        /**
         * 每次扩容增加的线程数
         */
        private int expansionThreadOneTime = 1;

        /**
         * 每次缩容增加的线程数
         */
        private int shrinkageThreadOneTime = 1;

        // ----

        /**
         * 是否支持动态调节
         */
        private boolean supportScale;

        /**
         * 扩容触发条件：队列中的任务数
         */
        private int expansionOnTaskCount = 0;

        /**
         * 缩容触发条件：队列中的任务数
         */
        private int shrinkageOnTaskCount = 0;

        private ScaleRule() {
        }

        public static ScaleRule newRule() {
            return new ScaleRule();
        }

        public boolean isSupportScale() {
            return supportScale;
        }

        public int getExpansionOnTaskCount() {
            return expansionOnTaskCount;
        }

        public int getShrinkageOnTaskCount() {
            return shrinkageOnTaskCount;
        }

        public ScaleRule expansionDuration(Duration expansionDuration) {
            Assert.isTrue(!expansionDuration.isZero() && !expansionDuration.isNegative(),
                "expansionDuration must > 0");
            this.expansionDuration = expansionDuration;
            return this;
        }

        public ScaleRule shrinkageDuration(Duration shrinkageDuration) {
            Assert.isTrue(!shrinkageDuration.isZero() && !shrinkageDuration.isNegative(),
                "shrinkageDuration must > 0");
            this.shrinkageDuration = shrinkageDuration;
            return this;
        }

        public ScaleRule expansionLoadFactor(float expansionLoadFactor) {
            Assert.isTrue(expansionLoadFactor >= 0 && expansionLoadFactor <= 1,
                "expansionLoadFactor must in range[0,1]");
            this.expansionLoadFactor = expansionLoadFactor;
            return this;
        }

        public ScaleRule shrinkageLoadFactor(float shrinkageLoadFactor) {
            Assert.isTrue(shrinkageLoadFactor >= 0 && shrinkageLoadFactor <= 1,
                "shrinkageLoadFactor must in range[0,1]");
            this.shrinkageLoadFactor = shrinkageLoadFactor;
            return this;
        }

        public ScaleRule expansionThreadOneTime(int expansionThreadOneTime) {
            Assert.isTrue(expansionThreadOneTime > 1, "expansionThreadOneTime must > 1");
            this.expansionThreadOneTime = expansionThreadOneTime;
            return this;
        }

        public ScaleRule shrinkageThreadOneTime(int shrinkageThreadOneTime) {
            Assert.isTrue(shrinkageThreadOneTime > 1, "shrinkageThreadOneTime must > 1");
            this.shrinkageThreadOneTime = shrinkageThreadOneTime;
            return this;
        }

        public Duration getExpansionDuration() {
            return expansionDuration;
        }

        public Duration getShrinkageDuration() {
            return shrinkageDuration;
        }

        public float getExpansionLoadFactor() {
            return expansionLoadFactor;
        }

        public float getShrinkageLoadFactor() {
            return shrinkageLoadFactor;
        }

        public int getExpansionThreadOneTime() {
            return expansionThreadOneTime;
        }

        public int getShrinkageThreadOneTime() {
            return shrinkageThreadOneTime;
        }



        public void check(){
            Assert.isTrue(expansionLoadFactor >= shrinkageLoadFactor,
                "expansionLoadFactor(" + expansionDuration + ") < shrinkageLoadFactor(" + shrinkageLoadFactor + ")");
            Assert.isTrue(expansionLoadFactor >= shrinkageLoadFactor,
                "expansionLoadFactor(" + expansionLoadFactor + ") < shrinkageLoadFactor(" + shrinkageLoadFactor + ")");
        }

        /**
         * 根据线程池参数调整
         *
         * @param coreSize core
         * @param maxSize  max
         * @param queueNum queueNum 队列需要有长度限制，若过大则调整的意义就没有了
         */
        public void adjust(int coreSize, int maxSize, int queueNum){
            check();
            int difference = maxSize - coreSize;
            expansionThreadOneTime = Math.min(expansionThreadOneTime, difference);
            shrinkageThreadOneTime = Math.min(shrinkageThreadOneTime, difference);
            expansionOnTaskCount = (int) (queueNum * expansionLoadFactor);
            shrinkageOnTaskCount = (int) (queueNum * shrinkageLoadFactor);
            supportScale = coreSize < maxSize && expansionOnTaskCount > shrinkageOnTaskCount;
        }

    }
}
