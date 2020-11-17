package org.shoulder.core.lock;

import org.shoulder.core.lock.impl.MemoryLock;

import java.time.Duration;

/**
 * 集群锁代理
 * - 通过该代理获取集群锁，保证每个服务示例在获取同一个资源时，最多只有一个线程尝试获取集群锁，以减少并发时锁冲突
 * - 可重入次数只需持有者统计即可，在此统计
 * - 分段加锁、避免持锁宕机（往往因使用者申请长时间锁导致）
 * - 锁续命，避免峰值/卡顿时意外释放
 * - 持有者才能释放，在此监管
 *
 * @author lym
 */
public class ServerLockAcquireProxy implements ServerLock {

    /**
     * 最多阻塞 1 分钟，1分钟后抛异常
     */
    public static final Duration DEFAULT_WAIT_DURATION = Duration.ofMillis(1);

    private ServerLock delegate;

    private ServerLock memoryLock = new MemoryLock();

    public ServerLockAcquireProxy(ServerLock delegate) {

    }

    @Override
    public boolean tryLock(LockInfo lockInfo, Duration maxBlockTime) throws InterruptedException {
        return delegate.tryLock(lockInfo, maxBlockTime);
    }

    @Override
    public boolean tryLock(LockInfo lockInfo) {
        return delegate.tryLock(lockInfo);
    }

    @Override
    public LockInfo getLockInfo(String resource) {
        return delegate.getLockInfo(resource);
    }

    @Override
    public void unlock(String resource, String token) {
        delegate.unlock(resource, token);
    }


}
