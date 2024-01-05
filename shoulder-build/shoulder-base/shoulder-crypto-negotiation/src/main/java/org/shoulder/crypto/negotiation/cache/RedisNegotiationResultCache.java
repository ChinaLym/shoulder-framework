package org.shoulder.crypto.negotiation.cache;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 密钥协商结果缓存
 *
 * @author lym
 */
public class RedisNegotiationResultCache implements NegotiationResultCache {

    private static final String DEFAULT_CLIENT_KEY_PREFIX = "negotiation:asClient:";
    private static final String DEFAULT_SERVER_KEY_PREFIX = "negotiation:asServer:";
    /**
     * 客户端存储安全会话信息缓存的key前缀
     */
    private final String clientKeyPrefix;
    /**
     * 服务端存储安全会话信息缓存的key前缀
     */
    private final String serverKeyPrefix;
    private RedisTemplate<String, Object> redisTemplate;

    public RedisNegotiationResultCache(RedisTemplate<String, Object> redisTemplate) {
        this(redisTemplate, DEFAULT_CLIENT_KEY_PREFIX, DEFAULT_SERVER_KEY_PREFIX);
    }

    public RedisNegotiationResultCache(RedisTemplate<String, Object> redisTemplate,
                                       String clientKeyPrefix, String serverKeyPrefix) {
        this.redisTemplate = redisTemplate;
        this.clientKeyPrefix = clientKeyPrefix;
        this.serverKeyPrefix = serverKeyPrefix;
    }

    @Override
    public void put(@Nonnull String cacheKey, @Nonnull NegotiationResult negotiationResult, boolean asClient) {
        String key = buildCacheKey(cacheKey, asClient);
        redisTemplate.opsForValue().set(
            key, JsonUtils.toJson(negotiationResult),
            negotiationResult.getExpireTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS
        );
    }


    @Override
    @Nullable
    public NegotiationResult get(String cacheKey, boolean asClient) {
        String key = buildCacheKey(cacheKey, asClient);

        Object obj = redisTemplate.opsForValue().get(key);
        if (obj == null) {
            return null;
        }
        return JsonUtils.parseObject(String.valueOf(obj), NegotiationResult.class);
    }

    @Override
    public void delete(String cacheKey, boolean asClient) {
        String key = buildCacheKey(cacheKey, asClient);
        redisTemplate.delete(key);
    }


    private String buildCacheKey(String cacheKey, boolean asClient) {
        return (asClient ? clientKeyPrefix : serverKeyPrefix) + cacheKey;
    }

}
