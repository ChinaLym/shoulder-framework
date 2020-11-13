package org.shoulder.core.lock;

import java.time.Duration;

/**
 * 全局锁，以支持应用集群
 *
 * @author lym
 */
public interface ServerLock {

    /**
     * 阻塞获取锁，直至成功
     *
     * @param lockInfo 锁信息
     */
    void lock(LockInfo lockInfo);

    /**
     * 阻塞获取锁，带超时时间
     *
     * @param lockInfo     锁信息
     * @param maxBlockTime 最大阻塞时长
     * @return 是否获取到锁
     * @throws InterruptedException 未获取到锁，被中断
     */
    boolean lock(LockInfo lockInfo, Duration maxBlockTime) throws InterruptedException;

    /**
     * 尝试获取锁，若未获取到则直接返回 false
     *
     * @param lockInfo 锁
     * @return 是否获取到
     */
    boolean tryLock(LockInfo lockInfo);


    /**
     * 尝试全局锁
     *
     * @param toLockResource 要锁的内容
     * @param holdDuration   持有时间
     * @return 是否加锁成功
     */
    default boolean tryLock(String toLockResource, Duration holdDuration) {
        String threadIndex = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        return tryLock(toLockResource, threadIndex, holdDuration);
    }

    /**
     * 尝试全局锁
     *
     * @param toLockResource 要锁的内容
     * @param valueEx        特殊值，避免线程之间勿释放，会默认在最前面拼上线程标识
     * @param holdDuration   持有时间
     * @return 是否加锁成功
     */
    boolean tryLock(String toLockResource, String valueEx, Duration holdDuration);


    /**
     * 是否持有锁
     *
     * @param toLockResource 要锁的内容
     * @param valueEx        锁凭证
     * @return 是否持有锁
     */
    boolean holdLock(String toLockResource, String valueEx);


    /**
     * 释放锁，若未持锁，则不做任何事情
     *
     * @param toLockResource toLockResource
     * @param valueEx        d
     */
    void release(String toLockResource, String valueEx);

}
