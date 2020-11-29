package org.shoulder.crypto.negotiation.cache;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * 密钥协商结果缓存
 *
 * @author lym
 */
public class RedisKeyNegotiationCache implements KeyNegotiationCache {

    private RedisTemplate<String, Object> redisTemplate;

    private String clientKeyPrefix;

    private String serverKeyPrefix;

    public RedisKeyNegotiationCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        clientKeyPrefix = ".negotiation:asClient:";
        serverKeyPrefix = ".negotiation:asServer:";
    }


    @Override
    public void put(@Nonnull String cacheKey, @Nonnull KeyExchangeResult keyExchangeResult, boolean asClient) {
        redisTemplate.opsForValue().set(
            (asClient ? clientKeyPrefix : serverKeyPrefix) + cacheKey,
            JsonUtils.toJson(keyExchangeResult),
            keyExchangeResult.getExpireTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS
        );
    }


    @Override
    @Nullable
    public KeyExchangeResult get(String cacheKey, boolean asClient) {
        String keyPrefix = asClient ?
            clientKeyPrefix : serverKeyPrefix;
        String key = keyPrefix + cacheKey;

        Object obj = redisTemplate.opsForValue().get(key);
        if (obj == null) {
            return null;
        }
        return JsonUtils.toObject(String.valueOf(obj), KeyExchangeResult.class);
    }


}
