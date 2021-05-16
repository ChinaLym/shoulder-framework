package org.shoulder.crypto.asymmetric.store.impl;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * RSA 密钥对存储-Redis 存储，适合应用支持集群部署的场景
 * 如果不使用过期时间等redis特有操作，可以通过双层缓存优化访问速度
 *
 * @author lym
 */
public class RedisKeyPairCache implements KeyPairCache {

    /**
     * 密钥对缓存前缀
     */
    private final String keyPrefix;
    /**
     * 注意维护应用隔离
     */
    private RedisTemplate<String, String> redisTemplate;

    public RedisKeyPairCache(StringRedisTemplate redisTemplate) {
        this(redisTemplate, "crypto:asymmetric:");
    }

    public RedisKeyPairCache(StringRedisTemplate redisTemplate, String keyPrefix) {
        this.keyPrefix = keyPrefix;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(String id, @Nonnull KeyPairDto keyPairDto) {
        String key = addRedisPrefix(id);
        // 为了安全和性能，仅将私钥加密，避免已知明文攻击
        String kpJson = JsonUtils.toJson(keyPairDto);
        if (keyPairDto.getExpireTime() != null) {
            long duration = ChronoUnit.MILLIS.between(Instant.now(), keyPairDto.getExpireTime());
            redisTemplate.opsForValue().set(key, kpJson, duration, TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set(key, kpJson);
        }

    }

    @Override
    public boolean putIfAbsent(String id, @Nonnull KeyPairDto keyPairDto) {
        String key = addRedisPrefix(id);
        // 为了安全和性能，仅将私钥加密，避免已知明文攻击
        String kpJson = JsonUtils.toJson(keyPairDto);
        if (keyPairDto.getExpireTime() != null) {
            long duration = ChronoUnit.MILLIS.between(Instant.now(), keyPairDto.getExpireTime());
            return redisTemplate.opsForValue().setIfAbsent(key, kpJson, duration, TimeUnit.MILLISECONDS);
        } else {
            return redisTemplate.opsForValue().setIfAbsent(key, kpJson);
        }
    }

    @Nonnull
    @Override
    public KeyPairDto get(String id) throws NoSuchKeyPairException {
        String key = addRedisPrefix(id);
        String cipherKp = redisTemplate.opsForValue().get(key);
        if (cipherKp == null) {
            throw new NoSuchKeyPairException("not such keyPair id= " + id);
        }
        KeyPairDto keyPairDto = JsonUtils.parseObject(cipherKp, KeyPairDto.class);
        if (StringUtils.isEmpty(keyPairDto.getVk())) {
            // 数据完整性遭到外界恶意破坏，不应继续使用
            throw new NoSuchKeyPairException("KeyPair.privateKey is empty, id= " + id);
        }
        return keyPairDto;
    }


    private String addRedisPrefix(String id) {
        return keyPrefix + id;
    }

}
