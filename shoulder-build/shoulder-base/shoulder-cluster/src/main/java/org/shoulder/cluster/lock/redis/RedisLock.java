package org.shoulder.cluster.lock.redis;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.core.lock.AbstractDistributeLock;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ServerLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.Nullable;

import java.util.Collections;

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

    public static final String SPLIT = "__";

    /**
     * 服务实例唯一标识
     */
    private String instanceId = null;


    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        log.info("RedisLock SET currentInstanceId=" + instanceId);
    }

    /**
     * todo 这里为了保证最轻量化，只存储了部分信息，考虑改为使用 hash 存储
     * @param resource 资源
     * @return 资源对应的锁信息
     */
    @Override
    @Nullable
    public LockInfo getLockInfo(String resource) {
        return new LockInfo(resource);
    }

    @Override
    public boolean tryLock(LockInfo lockInfo) {
        log.debug("Try lock [{}].", lockInfo.getResource());
        boolean success = Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(lockInfo.getResource(),
            genLockValue(lockInfo.getToken()), lockInfo.getHoldTime()));
        log.info("Lock [{}] {} for {}.", lockInfo.getResource(), lockInfo.getHoldTime(), success ? "SUCCESS" : "FAIL");
        return success;
    }

    @Override
    public boolean holdLock(String resource, String token) {
        return genLockValue(token).equals(redis.opsForValue().get(resource));
    }

    @Override
    public void unlock(String resource, String token) {
        Long result = redis.execute(releaseLockScript(), Collections.singletonList(resource),
            genLockValue(token));
        if (result == null || 1 != result) {
            log.debug("invalid release operation: resource={}, token={}", resource, token);
        } else {
            log.debug("Released lock [{}].", resource);
        }
    }

    /**
     * 附带拥有者标识
     *
     * @param token 令牌
     * @return 添加了实例标识
     */
    private String genLockValue(String token) {
        return instanceId + SPLIT + (StringUtils.isNotBlank(token) ? token : "");
    }

    private RedisScript<Long> releaseLockScript() {
        final String script = "local value = ARGV[1];\n" +
            "if redis.call('GET', KEYS[1]) ~= value then \n" +
            "\treturn -1; \n" +
            "else \n" +
            "\tredis.call('DEL', KEYS[1]);\n" +
            "\treturn 1;\n" +
            "\tend";
        return new DefaultRedisScript<>(script, Long.class);
    }
}
