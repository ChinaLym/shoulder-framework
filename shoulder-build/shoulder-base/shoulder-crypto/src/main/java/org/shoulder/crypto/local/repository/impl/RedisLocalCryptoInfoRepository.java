package org.shoulder.crypto.local.repository.impl;

import org.shoulder.crypto.local.entity.LocalCryptoMetaInfo;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用 redis 作为存储
 * 适合只加解密运行时数据或者 redis 中数据，大多数情况下加密元数据最好要持久化存储 {@link JdbcLocalCryptoInfoRepository}
 *
 * @author lym
 */
public class RedisLocalCryptoInfoRepository implements LocalCryptoInfoRepository {

    private final String keyPrefix;

    private RedisTemplate<String, Object> redisTemplate;

    public RedisLocalCryptoInfoRepository(RedisTemplate<String, Object> redisTemplate) {
        this(redisTemplate, "crypto:local");
    }

    public RedisLocalCryptoInfoRepository(RedisTemplate<String, Object> redisTemplate, String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void save(@Nonnull LocalCryptoMetaInfo localCryptoMetaInfo) {
        boolean set = redisTemplate.opsForHash().putIfAbsent(getCacheId(), localCryptoMetaInfo.getHeader(), localCryptoMetaInfo);
        if (!set) {
            throw new IllegalStateException("appId, markHeader exist, maybe another instance has init.");
        }
    }

    @Override
    public LocalCryptoMetaInfo get(@Nonnull String appId, @Nonnull String markHeader) {
        return (LocalCryptoMetaInfo) redisTemplate.opsForHash().get(getCacheId(), markHeader);
    }

    @Override
    @Nonnull
    public List<LocalCryptoMetaInfo> get(@Nonnull String appId) {
        List<Object> objects = redisTemplate.opsForHash().values(getCacheId());
        if (CollectionUtils.isEmpty(objects)) {
            return Collections.emptyList();
        }
        List<LocalCryptoMetaInfo> localCryptoMetaInfoList = new LinkedList<>();
        for (Object obj : objects) {
            LocalCryptoMetaInfo aesInfo = (LocalCryptoMetaInfo) obj;
            localCryptoMetaInfoList.add(aesInfo);
        }
        return localCryptoMetaInfoList;
    }


    public String getCacheId() {
        return keyPrefix;
    }

    /**
     * 支持集群
     */
    @Override
    public boolean supportCluster() {
        return true;
    }

}
