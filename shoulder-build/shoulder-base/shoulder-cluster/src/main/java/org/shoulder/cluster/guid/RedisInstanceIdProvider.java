package org.shoulder.cluster.guid;

import jakarta.annotation.Nonnull;
import org.shoulder.core.concurrent.ShoulderPeriodicTask;
import org.shoulder.core.concurrent.Threads;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.guid.AbstractInstanceIdProvider;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.AddressUtils;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 基于缓存的实现 (非幂等)
 * 执行 LUA 脚本的 redisTemplate.valueSerializer 不能为 jdk 的策略
 *
 * @author lym
 * @see <a href="https://redis.io/docs/latest/commands/zrem/">Redis 文档</a>
 */
@SuppressWarnings("rawtypes, unchecked")
public class RedisInstanceIdProvider extends AbstractInstanceIdProvider implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    private final Logger log = ShoulderLoggers.SHOULDER_DEFAULT;

    /**
     * redis zset keyName
     */
    private final String idAssignCacheKey;

    /**
     * redis hash keyName
     */
    private final String machineInfoKeyPrefix;

    /**
     * 最大可以申请到的 instanceId
     */
    private final long maxId;

    /**
     * instanceId 心跳续期周期
     */
    private final Duration heartbeatPeriod;

    /**
     * redis 抢占 instantId 的最小时间间隔，如果超过这个时间没有续期，则认为该实例已经挂掉，需要重新获取，并且该 instantId 可被其他机器抢占
     */
    private final long expiredSeconds;

    /**
     * 不能是 StringRedisTemplate
     */
    private final RedisTemplate redis;

    /**
     * 当前营业是否停止
     */
    private volatile boolean alreadyStop = false;

    /**
     * 该 instanceId 操作 token
     */
    final String token = StringUtils.uuid32();

    public RedisInstanceIdProvider(String idAssignCacheKey, String machineInfoKeyPrefix, long maxId, Duration heartbeatPeriod, Duration expiredPeriod, RedisTemplate redisTemplate) {
        this.idAssignCacheKey = idAssignCacheKey;
        this.machineInfoKeyPrefix = machineInfoKeyPrefix;
        this.maxId = maxId;
        this.heartbeatPeriod = heartbeatPeriod;
        this.expiredSeconds = expiredPeriod.getSeconds();
        this.redis = redisTemplate;
    }

    /**
     * @return id
     */
    @Override
    protected long assignInstanceId() {
        // zset[instanceId-最后心跳时间]
        // 1. 优先取空着的id，2. 其次取最旧不心跳的id（取前需要判断最后心跳时间久于 expiredPeriod）
        final String luaScriptForAssignId = """
            -- KEYS[1]: idAssignCacheKey (ZSET)
            -- ARGV[1]: maxInstanceId (INT)
            -- ARGV[2]: expiredSeconds (INT)
            
            -- -1 no available expired id; -2 find expired but op fail
            local instanceId = -1
            local currentSecondStamp = redis.call('time')[1]
            
            -- STEP_1: try to find a new id
            for i = 1, tonumber(ARGV[1]) do
                if not redis.call("ZSCORE", KEYS[1], i) then
                    local opResult = redis.call("ZADD", KEYS[1], currentSecondStamp, i)
                    if(opResult == 1) then
                        instanceId = i
                        break
                    end
                end
            end
            
            if instanceId == -1 then
                -- STEP_2: try to find an expired id
                local oldest = redis.call("ZRANGE", KEYS[1], 0, 0, "WITHSCORES")
                if #oldest > 0 and tonumber(oldest[2]) < currentSecondStamp - tonumber(ARGV[2]) then
                    instanceId = oldest[1]
                    local opResult = redis.call("ZADD", KEYS[1], "CH", currentSecondStamp, instanceId)
                    if(opResult ~= 1) then
                        instanceId = -2
                    end
                end
            end
            
            return instanceId
            """;

        RedisScript<Long> tryInstanceIdScript = new DefaultRedisScript<>(luaScriptForAssignId, Long.class);
        long instanceIdFromRedis = (Long)redis.execute(tryInstanceIdScript, List.of(idAssignCacheKey), maxId, expiredSeconds);
        if (instanceIdFromRedis > 0) {
            AssertUtils.isTrue(instanceIdFromRedis <= maxId, CommonErrorCodeEnum.CODING);
            log.info("redisInstanceIdProvider.assignInstanceId SUCCESS. instanceId={}", instanceIdFromRedis);
        } else {
            String msg = instanceIdFromRedis == -1 ? "instance full, please expand maxId or clean expired." : "redisOpFail, please retry.";
            log.error("redisInstanceIdProvider.assignInstanceId FAIL({}): maxId={}, expiredSeconds={}s", msg, maxId, expiredSeconds);
        }

        return instanceIdFromRedis;
    }

    @Override
    public void onApplicationEvent(@Nonnull ContextRefreshedEvent event) {
        ShoulderPeriodicTask hearBeatTask = new ShoulderPeriodicTask() {

            @Override public String getTaskName() {
                return "RedisInstanceIdProviderHeartBeat";
            }

            @Override public void process() {
                String mac = AddressUtils.getMac();
                String ip = AddressUtils.getIp();
                String hostname = AddressUtils.getHostname();

                // 获取 info，检查和当前 mac 地址相同才更新
                final String luaScript =
                    """
                            -- KEYS[1]: idAssignCacheKey (ZSET)
                            -- KEYS[2]: infoCacheKey (HASH)
                            -- ARGV[1]: currentInstantId (INT)
                            -- ARGV[2]: expiredSeconds (INT)
                            -- ARGV[3]: token (INT)
                            -- ARGV[4]: MAC (STR)
                            -- ARGV[5]: IP (STR)
                            -- ARGV[6]: HOST (STR)
                            
                            local tokenInfo = redis.call('HGET', KEYS[2], "token");
                            local tokenRight = #macInfo > 0 and tokenInfo[1] == ARGV[3]
                            if tokenInfo ~=nil and not tokenRight then
                                return -2
                            end
                            
                            local currentSecondStamp = redis.call('time')[1];
                            local lastHeartBeatTimeStamp = redis.call('ZSCORE', KEYS[1], ARGV[1]);
                            local heartRight = #lastHeartBeatTimeStamp > 0 and lastHeartBeatTimeStamp < currentSecondStamp - tonumber(ARGV[2])
                            if not heartRight then
                                return -3
                            end
                            
                            redis.call('HSET', KEYS[2], "mac", ARGV[4]);
                            redis.call('HSET', KEYS[2], "ip", ARGV[5]);
                            redis.call('HSET', KEYS[2], "host", ARGV[6]);
                            local opResult = redis.call('ZADD', KEYS[1], "CH", currentSecondStamp, ARGV[1]);
                            if(opResult ~= 1) then
                                instanceId = -1
                            else
                                return 1
                            end
                            """;

                try {
                    RedisScript<Long> heartbeatScript = new DefaultRedisScript<>(luaScript, Long.class);
                    long instanceId = getCurrentInstanceId();
                    long resultCode = (Long) redis.execute(heartbeatScript, List.of(idAssignCacheKey, machineInfoKeyPrefix), instanceId, expiredSeconds, token, mac, ip, hostname);

                    LuaExecutionResult result = LuaExecutionResult.fromResultCode(resultCode);
                    if (result == LuaExecutionResult.SUCCESS) {
                        log.debug("redisInstanceIdProvider.heartbeat SUCCESS.");
                    } else if (Objects.requireNonNull(result) == LuaExecutionResult.FAIL_ALLOW_RETRY) {
                        log.warn("redisInstanceIdProvider.heartbeat FAIL(willRetry): instanceId={}", instanceId);
                    } else {
                        log.error("redisInstanceIdProvider.heartbeat FATAL!({}): instanceId={}, token={}", result.name(), instanceId, token);
                    }
                } catch (Exception e) {
                    log.warn("redisInstanceIdProvider.heartbeat EXCEPTION(will retry)!", e);
                }
            }

            @Override public Instant calculateNextRunTime(Instant now, int runCount) {
                // 每分钟执行一次
                return alreadyStop ? NO_NEED_EXECUTE : now.plus(heartbeatPeriod);
            }
        };
        Threads.schedule(hearBeatTask);
    }

    @Override
    public void destroy() {
        releaseInstanceId();
    }

    private void releaseInstanceId() {
        alreadyStop = true;
        final String luaScript =
                """
                            -- KEYS[1]: idAssignCacheKey (ZSET)
                            -- KEYS[2]: infoCacheKey (HASH)
                            -- ARGV[1]: currentInstantId (INT)
                            -- ARGV[2]: expiredSeconds (INT)
                            -- ARGV[3]: token (INT)
                            
                            local tokenInfo = redis.call('HGET', KEYS[2], "token");
                            local tokenRight = #macInfo > 0 and tokenInfo[1] == ARGV[3]
                            if tokenInfo ~=nil and not tokenRight then
                                return -2
                            end
                            
                            local currentSecondStamp = redis.call('time')[1];
                            local lastHeartBeatTimeStamp = redis.call('ZSCORE', KEYS[1], ARGV[1]);
                            local heartRight = #lastHeartBeatTimeStamp > 0 and lastHeartBeatTimeStamp < currentSecondStamp - tonumber(ARGV[2])
                            if not heartRight then
                                return -3
                            end
                            
                            redis.call('DEL', KEYS[2]);
                            local opResult = redis.call('ZREM', KEYS[1], ARGV[1]);
                            if(opResult ~= 1) then
                                instanceId = -1
                            else
                                return 1
                            end
                            """;
        RedisScript<Long> releaseInstanceIdScript = new DefaultRedisScript<>(luaScript, Long.class);

        long instanceId = getCurrentInstanceId();
        long resultCode = (Long) redis.execute(releaseInstanceIdScript, List.of(idAssignCacheKey), instanceId, expiredSeconds, token);

        LuaExecutionResult result = LuaExecutionResult.fromResultCode(resultCode);
        if (result == LuaExecutionResult.SUCCESS) {
            log.debug("redisInstanceIdProvider.releaseInstanceId SUCCESS.");
        } else if (Objects.requireNonNull(result) == LuaExecutionResult.FAIL_ALLOW_RETRY) {
            log.warn("redisInstanceIdProvider.releaseInstanceId FAIL(willRetry): instanceId={}", instanceId);
        } else {
            log.error("redisInstanceIdProvider.releaseInstanceId FATAL!({}): instanceId={}, token={}", result.name(), instanceId, token);
        }
    }

    /**
     * Redis LUA 脚本操作结果
     */
    private enum LuaExecutionResult {
        SUCCESS(1),
        FAIL_ALLOW_RETRY(-1),
        FAIL_FOR_PRIVILEGE_DENIED(-2),
        FAIL_FOR_EXPIRED(-3),
        FAIL_FOR_UNKNOWN_REASON(-99),
        ;

        final int errorCode;

        LuaExecutionResult(int errorCode) {
            this.errorCode = errorCode;
        }

        public static LuaExecutionResult fromResultCode(long resultCode) {
            if(resultCode >= 1) {
                return SUCCESS;
            }
            for (LuaExecutionResult value : values()) {
                if (value.errorCode == resultCode) {
                    return value;
                }
            }
            return FAIL_FOR_UNKNOWN_REASON;
        }

    }

}
