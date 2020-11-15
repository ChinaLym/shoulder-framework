package org.shoulder.core.lock;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通过装饰者模式实现重入次数计数
 * <p>
 * 因为每次重入时，只需判断是否持有锁即可，无需更新中间件中的值，减少网络通信与写开销
 *
 * @author lym
 */
public class ReentrantServerLock implements ServerLock {

    private ServerLock delegate;

    /**
     * 当前进程持有的锁的重入计数器
     * 锁标识 - 重入次数
     */
    private ConcurrentMap<String, AtomicInteger> reentrantCountMap = new ConcurrentHashMap<>();

    public ReentrantServerLock(ServerLock delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean tryLock(LockInfo lockInfo, Duration maxBlockTime) throws InterruptedException {
        if (delegate.holdLock(lockInfo)) {
            reentrantCountMap.get(lockInfo.getResource()).incrementAndGet();
            return true;
        } else {
            boolean lock = delegate.tryLock(lockInfo, maxBlockTime);
            if (lock) {
                reentrantCountMap.put(lockInfo.getResource(), new AtomicInteger(0));
            }
            return lock;
        }
    }

    @Override
    public boolean tryLock(LockInfo lockInfo) {
        if (delegate.holdLock(lockInfo)) {
            reentrantCountMap.get(lockInfo.getResource()).incrementAndGet();
            return true;
        } else {
            boolean lock = delegate.tryLock(lockInfo);
            if (lock) {
                reentrantCountMap.put(lockInfo.getResource(), new AtomicInteger(0));
            }
            return lock;
        }
    }

    @Override
    public LockInfo getLockInfo(String resource) {
        return delegate.getLockInfo(resource);
    }

    @Override
    public boolean holdLock(String resource, String token) {
        return delegate.holdLock(resource, token);
    }

    @Override
    public void unlock(String resource, String token) {
        if (holdLock(resource, token)) {
            if (reentrantCountMap.get(resource).decrementAndGet() == 0) {
                delegate.unlock(resource, token);
            }
        }
    }
}
