package org.shoulder.cluster.guid;

import jakarta.annotation.Nonnull;
import org.shoulder.core.concurrent.Threads;
import org.shoulder.core.guid.AbstractInstanceIdProvider;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

/**
 * 基于缓存的实现 (非幂等)
 * 执行 LUA 脚本的 redisTemplate.valueSerializer 不能为 jdk 的策略
 *
 * @author lym
 */
@SuppressWarnings("rawtypes, unchecked")
public class RedisInstanceIdProvider extends AbstractInstanceIdProvider implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    private final Logger log = ShoulderLoggers.SHOULDER_DEFAULT;

    /**
     * redis key
     */
    private final String idAssignCacheKey;

    /**
     * 最大可以申请到的 instanceId
     */
    private final long maxId;

    /**
     * 不能是 StringRedisTemplate
     */
    private final RedisTemplate redis;

    public RedisInstanceIdProvider(String idAssignCacheKey, long maxId, RedisTemplate redisTemplate) {
        this.idAssignCacheKey = idAssignCacheKey;
        this.maxId = maxId;
        this.redis = redisTemplate;
    }

    /**
     * @return id
     */
    @Override
    protected long assignInstanceId() {
        // 遍历目标 map 所有机器标识 <id-最后一次心跳时间>
        // 心跳时间大于 10 分钟的，抢占，并续命心跳
        // 添加一项
        final String luaScript =
                "local currentTime=redis.call('time')[1];\n" +
                        "local existsKeys = redis.call('hkeys', KEYS[1]);\n" +
                        // 第一台机器，hash 为空，直接使用 0
                        "if(existsKeys == nil or #existsKeys == nil)\n" +
                        "then\n" +
                        "   local opResult = redis.call('hsetnx', KEYS[1], 0, currentTime);\n" +
                        "   if(opResult == 1)\n" +
                        "   then\n" +
                        "       return 0;\n" +
                        "   else\n" +
                        "       return -2;\n" +
                        "   end;\n" +
                        "end;\n" +
                        // 机器数小于 maxId，直接使用机器数作为 id
                        "if #existsKeys < tonumber(ARGV[1])\n" +
                        "then\n" +
                        "   local id = #existsKeys;\n" +
                        "   local opResult = redis.call('hsetnx', KEYS[1], id, currentTime);\n" +
                        "   if(opResult == 1)\n" +
                        "   then\n" +
                        "       return id;\n" +
                        "   else\n" +
                        "       return -3;\n" +
                        "   end;\n" +
                        "end;\n" +
                        // 机器数 >= maxId，遍历前面的，看是否有很久（10min）不用的，抢占他
                        "for i=1,KEYS[1] do\n" +
                        "   if currentTime - redis.call('hget', KEYS[1], existsKeys[i]) > 900\n" +
                        "   then\n" +
                        "       local opResult = redis.call('hsetnx', KEYS[1], existsKeys[i], currentTime);\n" +
                        "       if(opResult == 1)\n" +
                        "       then\n" +
                        "           return existsKeys[i];\n" +
                        "       else\n" +
                        "           return -4;\n" +
                        "       end;\n" +
                        "   end;\n" +
                        "end;\n" +
                        "return -1;\n";
        RedisScript<Long> tryInstanceIdScript = new DefaultRedisScript<>(luaScript, Long.class);

        long result = (long) redis.execute(tryInstanceIdScript, List.of(idAssignCacheKey), maxId);
        if (result < 0) {
            log.error("redisInstanceIdProvider assignInstanceId FAIL({}): idAssignCacheName={}, maxId={}", result, idAssignCacheKey, maxId);
        } else {
            log.debug("redisInstanceIdProvider assignInstanceId SUCCESS.");
        }

        return result;
    }

    protected void heartbeat() {
        final String luaScript =
                """
                        local currentTime=redis.call('time')[1];
                        redis.call('hset', KEYS[1], ARGV[1], currentTime);
                        return 1;
                        """;
        RedisScript<Long> heartbeatScript = new DefaultRedisScript<>(luaScript, Long.class);

        try {
            long result = (long) redis.execute(heartbeatScript, List.of(idAssignCacheKey), super.getCurrentInstanceId());
            if (result == 1) {
                log.debug("redisInstanceIdProvider heartbeat SUCCESS.");
            } else {
                log.warn("redisInstanceIdProvider heartbeat FAIL: idAssignCacheName={}, instanceId={}", idAssignCacheKey, super.getCurrentInstanceId());
            }
        } catch (Exception e) {
            log.error("redisInstanceIdProvider heartbeat ex FAIL!", e);
        } finally {
            Threads.delay(this::heartbeat, Duration.ofMinutes(1));
        }

    }


    private void releaseInstanceId() {
        final String luaScript =
                """
                        redis.call('hdel', KEYS[1], ARGV[1]);
                        return 1;
                        """;
        RedisScript<Long> releaseInstanceIdScript = new DefaultRedisScript<>(luaScript, Long.class);

        long result = (long) redis.execute(releaseInstanceIdScript, List.of(idAssignCacheKey), super.getCurrentInstanceId());
        if (result == 1) {
            log.debug("releaseInstanceId SUCCESS.");
            instanceId = ILLEGAL;
        } else {
            log.debug("releaseInstanceId FAIL: idAssignCacheName={}, instanceId={}", idAssignCacheKey, super.getCurrentInstanceId());
        }
    }

    @Override
    public void onApplicationEvent(@Nonnull ContextRefreshedEvent event) {
        Threads.execute(this::heartbeat);
    }

    @Override
    public void destroy() throws Exception {
        releaseInstanceId();
    }
}
