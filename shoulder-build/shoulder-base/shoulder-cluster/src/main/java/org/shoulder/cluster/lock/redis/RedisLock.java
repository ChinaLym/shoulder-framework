package org.shoulder.cluster.lock.redis;

import org.shoulder.core.lock.AbstractDistributeLock;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ServerLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;

/**
 * 基于 Redis 的分布式锁
 * 适合 key 多变，锁量大，注意：可靠性由redis保证，而redis当且仅当在无从节点时才能保证不丢数据。
 * why not RedissonLock ? 虽然它提供了大量的工具包，但依赖了大量的 redis 的能力，若某一环节崩溃，更可能出现故障
 * 基础层，选择 keep it simple! 仅依赖 k-v 和原子性命令，提供基本实现，使用者可自行选择
 * why not RedLock ? 为了 redLock 专门提供 3 组隔离的 redis 集群代价太大得不偿失。
 *
 * @author lym
 */
public class RedisLock extends AbstractDistributeLock implements ServerLock {

    /**
     * redis 模板
     */
    private StringRedisTemplate redis;

    public RedisLock(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * @param resource 资源
     * @return 资源对应的锁信息
     */
    @Override
    @Nullable
    public LockInfo getLockInfo(String resource) {
        // todo get
        return null;
    }

    @Override
    public boolean tryLock(LockInfo lockInfo) {
        boolean locked = false;
        // todo lua 加锁脚本
        //redis.exec();
        log.debug("try lock {}! {}", locked ? "SUCCESS" : "FAIL", lockInfo);
        return locked;
    }

    @Override
    public boolean holdLock(String resource, String token) {
        // todo lua 查询
        return false;
    }

    @Override
    public void unlock(String resource, String token) {
        // todo lua 释放
    }

}
