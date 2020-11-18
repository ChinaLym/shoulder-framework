package org.shoulder.core.lock;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * 为基于 k-v 的分布式锁提供阻塞策略
 * 子类只需实现 getLockInfo、tryLock、unlock 即可通用的实现分布式锁
 * 互斥（基本条件）、防死锁（持锁宕机，框架解决）、可重入（由持锁端支持，框架解决）、高性能（使用者减少锁粒度、范围）
 *
 * @author lym
 */
public abstract class AbstractDistributeLock extends AbstractServerLock {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 重试等待时间间隔
     */
    protected Duration retryBlockTime = Duration.ofMillis(30);


    public Duration getRetryBlockTime() {
        return retryBlockTime;
    }

    public void setRetryBlockTime(Duration retryBlockTime) {
        this.retryBlockTime = retryBlockTime;
    }

    /**
     * 【阻塞时间精确性注意】因为每次尝试都与数据库交互，故也是耗时的，尤其是数据库繁忙时，举例：
     * 使用者期望最多阻塞5s，拿不到锁立即返回，结果阻塞5s加上轮询数据库耗时（不确定将阻塞多久，比如5s），导致总供阻塞了10s
     * 这是使用者意料之外的，故需要统计该时间
     * <p>
     * 框架实现-最多阻塞 expectMaxBlockTime + 一次尝试获取时间
     *
     * @param lockInfo           锁信息
     * @param exceptMaxBlockTime 等待获取锁最大阻塞时间，实际必然大于该值，框架实现尽量贴近该值
     * @return 是否获取成功
     * @throws InterruptedException 阻塞时被其他线程打断
     */
    @Override
    public boolean tryLock(LockInfo lockInfo, Duration exceptMaxBlockTime) throws InterruptedException {
        // 返回截止时间
        Instant startTime = Instant.now();
        Instant deadline = startTime.plus(exceptMaxBlockTime);
        for (int tryTimes = 0; !tryLock(lockInfo); tryTimes++) {
            // 剩余最长可等待时间
            Duration maxBlockTime = Duration.between(Instant.now(), deadline);
            if (maxBlockTime.isNegative() || maxBlockTime.isZero()) {
                // 剩余可等待时间 <= 0：达到最大时间且没有获取到
                log.info("try lock FAIL with {}! {}", exceptMaxBlockTime, lockInfo);
                return false;
            }
            // 取较小的
            Duration blockTime = retryBlockTime.compareTo(maxBlockTime) < 0 ? retryBlockTime : maxBlockTime;
            log.trace("try lock {} for {} times.", lockInfo.getResource(), tryTimes);
            // 阻塞，直至加锁成功
            Thread.sleep(blockTime.toMillis());
        }
        log.debug("try lock SUCCESS cost {}! {}", Duration.between(startTime, Instant.now()), lockInfo);
        return true;
    }

}
