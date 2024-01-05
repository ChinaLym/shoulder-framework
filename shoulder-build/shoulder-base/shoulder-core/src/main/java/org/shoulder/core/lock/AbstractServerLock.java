package org.shoulder.core.lock;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PreDestroy;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 全局锁抽象类，支持 jdk 的方法
 * 通过线程变量，保存token，模拟 JDK 锁
 *
 * @author lym
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public abstract class AbstractServerLock implements ServerLock {

    /**
     * 资源标识
     */
    private final String resource;

    private final ThreadLocal<LockInfo> lockInfoLocal;

    public AbstractServerLock() {
        this(UUID.randomUUID().toString());
    }

    public AbstractServerLock(String resource) {
        this.resource = resource;
        this.lockInfoLocal = ThreadLocal.withInitial(() -> new LockInfo(resource));
    }


    @Override
    public void lock() {
        lock(getThisLockNonNull());
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        while (true) {
            if (tryLock(getThisLockNonNull(), Duration.ofDays(1))) {
                return;
            }
        }
    }

    @Override
    public boolean tryLock() {
        return tryLock(getThisLockNonNull());
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return tryLock(getThisLockNonNull(), Duration.of(time, toTemporalUnit(unit)));
    }

    @Override
    public void unlock() {
        unlock(getThisLockNonNull());
    }

    /**
     * 获取当前锁对象，对应的锁
     *
     * @return 前锁对象，对应的锁
     */
    private LockInfo getThisLock() {
        return lockInfoLocal.get();
    }

    private LockInfo getThisLockNonNull() {
        LockInfo lockInfo = lockInfoLocal.get();
        AssertUtils.notNull(lockInfo, CommonErrorCodeEnum.CODING, "You are using not reccognized method, and internal lockInfo is null!");
        return lockInfo;
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
        lockInfoLocal.remove();
    }


    public String getResource() {
        return resource;
    }
}
