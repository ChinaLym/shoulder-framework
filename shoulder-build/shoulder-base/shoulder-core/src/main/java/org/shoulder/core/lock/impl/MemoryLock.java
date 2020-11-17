package org.shoulder.core.lock.impl;

import org.shoulder.core.lock.AbstractServerLock;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存锁，并非分布式的，使用场景：
 * 1. 预适配支持集群 / mock 本地调试
 * 2. 记录本进程获取的锁
 *
 * @author lym
 */
public class MemoryLock extends AbstractServerLock {

    /**
     * 持有的锁
     */
    private static ConcurrentHashMap<String, LockInfo> holdLocks = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(LockInfo lockInfo, Duration maxBlockTime) throws InterruptedException {
        Instant now = Instant.now();
        Instant maxBlockInstant = now.plus(maxBlockTime);
        while (true) {
            LockInfo oldLock;
            if ((oldLock = holdLocks.putIfAbsent(lockInfo.getResource(), lockInfo)) == null) {
                return true;
            }
            Duration ttl = Duration.between(Instant.now(),
                // 取更小的
                maxBlockInstant.compareTo(oldLock.getReleaseTime()) > 0 ? oldLock.getReleaseTime() : maxBlockInstant);

            synchronized (oldLock) {
                oldLock.wait(ttl.toMillis());
            }
            // 有人释放，重新尝试获取锁
            if (Instant.now().compareTo(maxBlockInstant) > 0) {
                // 达到最大阻塞时间加锁失败
                return false;
            }
        }
    }

    @Override
    public boolean tryLock(LockInfo lockInfo) {
        return holdLocks.putIfAbsent(lockInfo.getResource(), lockInfo) == null;
    }

    @Override
    public boolean holdLock(String resource, String token) {
        LockInfo oldLock = holdLocks.get(resource);
        if (oldLock == null) {
            // 没有人上锁
            return false;
        }
        // 如果 token 正确，则认为是自己上的锁
        return StringUtils.equals(token, oldLock.getToken());
    }

    @Override
    public LockInfo getLockInfo(String resource) {
        return holdLocks.get(resource);
    }

    @Override
    public void unlock(String resource, String token) {
        LockInfo oldLock = holdLocks.get(resource);
        if (oldLock == null) {
            // 没有人上锁
            return;
        }
        if (StringUtils.equals(token, oldLock.getToken())) {
            // 移除锁，同时唤醒所有等待该锁的线程
            oldLock = holdLocks.remove(resource);
            synchronized (oldLock) {
                oldLock.notifyAll();
            }
        }
        // token 不正确，无法释放
    }
}
