package org.shoulder.core.lock;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 全局锁抽象类，支持 jdk 的方法
 *
 * @author lym
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public abstract class AbstractServerLock implements ServerLock {

    /**
     * 保存前锁对象，对应的锁，以支持 jdk 的方法，禁止使用者自行使用，否则不安全
     */
    private static final ConcurrentHashMap<Object, LockInfo> UNSAFE_FOR_JDK_SUPPORT = new ConcurrentHashMap<>();

    private final ThreadLocal<String> OPERATION_TOKEN = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

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
        return UNSAFE_FOR_JDK_SUPPORT.computeIfAbsent(this, k -> new LockInfo(OPERATION_TOKEN.get()));
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
        OPERATION_TOKEN.remove();
    }

}
