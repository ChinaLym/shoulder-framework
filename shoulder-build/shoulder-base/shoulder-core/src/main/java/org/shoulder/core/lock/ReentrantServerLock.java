package org.shoulder.core.lock;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通过装饰者模式实现重入次数计数，让本不支持可重入的锁（如JdbcLock、RedisLock）也支持~
 * -- 为什么不把可重入次数写在外部存锁的 DB、Redis 里，而是锁信息、重入次数分开存放？？
 * <p>
 * 1. 性能：因为每次重入时，只需判断是否持有锁即可，无需更新中间件中的值，减少网络通信与写开销
 * 2. 通用：可以快速将可重入能力复用给其他类型锁，比如 zookeeper 等
 * 3. 安全：因该能力通用，故无需关心外部存储类型，而不必为不同存储写操作可重入次数的定制逻辑代码，消灭了潜在的bug
 * <p>
 *   注意，该类主要用于集群，单机模式的 JDKLock 本身就支持，故单机模式不需要本类包装，shoulder 也不会用它~
 *
 * @author lym
 */
public class ReentrantServerLock implements ServerLock {

    private ServerLock delegate;

    /**
     * 当前进程持有的锁的重入计数器
     * 锁标识 - 加锁的次数
     */
    private ConcurrentMap<String, AtomicInteger> reentrantCountMap = new ConcurrentHashMap<>();

    public ReentrantServerLock(ServerLock delegate) {
        this.delegate = delegate;
    }

    // --- JDK 定义的，但是 AbstractServerLock 定义的一定会调用到带 LockInfo 的lock---- 故不需要前后置方法
    // ---- 父类 ServerLock 的 default 方法不覆盖，因为还会调到其他不带 default 的接口

    @Override
    public boolean tryLock(LockInfo lockInfo, Duration exceptMaxBlockTime) throws InterruptedException {
        if (delegate.holdLock(lockInfo)) {
            reentrantCountMap.get(lockInfo.getResource()).incrementAndGet();
            return true;
        } else {
            boolean lock = delegate.tryLock(lockInfo, exceptMaxBlockTime);
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
                reentrantCountMap.put(lockInfo.getResource(), new AtomicInteger(1));
            }
            return lock;
        }
    }

    @Override
    public LockInfo getLockInfo(String resource) {
        // todo 可重入次数？
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

    public ServerLock getDelegate() {
        return delegate;
    }
}
