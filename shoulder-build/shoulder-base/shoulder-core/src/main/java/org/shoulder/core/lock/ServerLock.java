package org.shoulder.core.lock;

import org.shoulder.core.util.StringUtils;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 全局锁，以支持应用集群
 *
 * @author lym
 */
public interface ServerLock extends Lock {


    /**
     * 阻塞获取锁，直至成功
     *
     * @param lockInfo 锁信息
     */
    default void lock(LockInfo lockInfo) {
        while (true) {
            try {
                if (tryLock(lockInfo, ChronoUnit.FOREVER.getDuration())) {
                    return;
                }
            } catch (InterruptedException ignore) {
            }
        }
    }

    /**
     * 阻塞获取锁，带超时时间
     *
     * @param lockInfo     锁信息
     * @param maxBlockTime 最大阻塞时长
     * @return 是否获取到锁
     * @throws InterruptedException 未获取到锁，被中断
     */
    boolean tryLock(LockInfo lockInfo, Duration maxBlockTime) throws InterruptedException;

    /**
     * 尝试获取锁，若未获取到则直接返回 false
     *
     * @param lockInfo 锁信息
     * @return 是否获取到
     */
    boolean tryLock(LockInfo lockInfo);


    /**
     * 当前线程是否持有锁
     *
     * @param resource 要锁的内容
     * @param token    锁操作令牌
     * @return 是否持有锁
     */
    default boolean holdLock(String resource, String token) {
        LockInfo lockInfo = getLockInfo(resource);
        return lockInfo != null && StringUtils.equals(lockInfo.getToken(), token);
    }

    /**
     * 当前线程是否持有锁
     *
     * @param lockInfo 锁信息
     * @return 是否持有锁
     */
    default boolean holdLock(LockInfo lockInfo) {
        return holdLock(lockInfo.getResource(), lockInfo.getToken());
    }

    /**
     * 获取锁对应的信息
     *
     * @param resource 资源
     * @return 锁信息
     */
    @Nullable
    LockInfo getLockInfo(String resource);

    /**
     * 释放锁，若未持锁，则不做任何事情
     *
     * @param resource 锁定的资源
     * @param token    锁操作令牌
     */
    void unlock(String resource, String token);

    /**
     * 释放锁
     *
     * @param lockInfo 锁信息
     */
    default void unlock(LockInfo lockInfo) {
        unlock(lockInfo.getResource(), lockInfo.getToken());
    }

    /**
     * 尝试全局锁
     *
     * @param resource     要锁的内容
     * @param holdDuration 持有时间
     * @return 是否加锁成功
     */
    default boolean tryLock(String resource, Duration holdDuration) {
        LockInfo lockInfo = new LockInfo(resource, holdDuration);
        return tryLock(lockInfo);
    }

    /**
     * 尝试全局锁
     *
     * @param resource     要锁的内容
     * @param token        锁操作令牌
     * @param holdDuration 持有时间
     * @return 是否加锁成功
     */
    default boolean tryLock(String resource, String token, Duration holdDuration) {
        LockInfo lockInfo = new LockInfo(resource, holdDuration);
        lockInfo.setToken(token);
        return tryLock(lockInfo);
    }

    // ============================ JDK 接口的方法，默认不支持 ==========================

    /**
     * jdk 锁
     *
     * @see Lock#lock()
     */
    @Override
    default void lock() {
        throw new UnsupportedOperationException();
    }

    /**
     * jdk 锁
     * @see Lock#lockInterruptibly()
     */
    @Override
    default void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    /**
     * jdk 锁
     *
     * @see Lock#tryLock()
     */
    @Override
    default boolean tryLock() {
        throw new UnsupportedOperationException();
    }

    /**
     * jdk 锁
     * @param time 时间
     * @param unit 时间单位
     * @return 是否加锁成功
     * @throws InterruptedException 中断
     * @see Lock#tryLock()
     */
    @Override
    default boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    /**
     * jdk 释放锁
     *
     * @see Lock#unlock()
     */
    @Override
    default void unlock() {
        throw new UnsupportedOperationException();
    }

    /**
     * jdk 创建条件
     *
     * @see Lock#newCondition()
     */
    @Override
    default Condition newCondition() {
        throw new UnsupportedOperationException();
    }

}
