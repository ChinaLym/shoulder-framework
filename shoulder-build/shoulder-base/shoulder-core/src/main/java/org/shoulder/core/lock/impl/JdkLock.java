package org.shoulder.core.lock.impl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PreDestroy;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.util.StringUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通过juc提供的工具实现的，非分布式
 *
 * @author lym
 */
public class JdkLock implements ServerLock {

    /**
     * 持有的锁
     */
    private static ConcurrentHashMap<String, Lock> holdLocks = new ConcurrentHashMap<>();

    /**
     * 锁信息
     */
    private static ConcurrentHashMap<String, LockInfo> lockInfos = new ConcurrentHashMap<>();

    private LockInfo thisLock = new LockInfo(UUID.randomUUID().toString());

    @Override
    public boolean tryLock(LockInfo lockInfo, Duration exceptMaxBlockTime) throws InterruptedException {
        Lock lock = holdLocks.computeIfAbsent(lockInfo.getResource(), k -> new ReentrantLock());
        if (lock.tryLock(exceptMaxBlockTime.toMillis(), TimeUnit.MILLISECONDS)) {
            lockInfos.put(lockInfo.getResource(), lockInfo);
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLock(LockInfo lockInfo) {
        Lock lock = holdLocks.computeIfAbsent(lockInfo.getResource(), k -> new ReentrantLock());
        if (lock.tryLock()) {
            lockInfos.put(lockInfo.getResource(), lockInfo);
            return true;
        }
        return false;
    }

    @Override
    public boolean holdLock(String resource, String token) {
        LockInfo lockInfo = lockInfos.get(resource);
        if (lockInfo == null) {
            return false;
        }
        return StringUtils.equals(lockInfo.getToken(), token);
    }

    @Override
    public LockInfo getLockInfo(String resource) {
        return lockInfos.get(resource);
    }

    @Override
    public void unlock(String resource, String token) {
        Lock oldLock = holdLocks.get(resource);
        if (oldLock == null) {
            // 没有人上锁
            return;
        }
        oldLock.unlock();
    }


    @Override
    public void lock() {
        lock(getThisLock());
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        while (true) {
            if (tryLock(getThisLock(), Duration.ofDays(1))) {
                return;
            }
        }
    }

    @Override
    public boolean tryLock() {
        return tryLock(getThisLock());
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return tryLock(getThisLock(), Duration.of(time, toTemporalUnit(unit)));
    }

    @Override
    public void unlock() {
        unlock(getThisLock());
    }

    /**
     * 获取当前锁对象，对应的锁
     *
     * @return 前锁对象，对应的锁
     */
    private LockInfo getThisLock() {
        return thisLock;
    }

    private TemporalUnit toTemporalUnit(@Nonnull TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new IllegalStateException();
        }
    }

    @PreDestroy
    public void preDestroy() {
        lockInfos.clear();
        holdLocks.clear();
    }

}
