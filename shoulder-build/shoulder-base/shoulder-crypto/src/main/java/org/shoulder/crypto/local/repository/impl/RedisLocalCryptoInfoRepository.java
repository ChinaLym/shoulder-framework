package org.shoulder.crypto.local.repository.impl;

import org.shoulder.crypto.local.entity.LocalCryptoInfoEntity;
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

    private static final String KEY_PREFIX = "aesInfo";
    private RedisTemplate<String, Object> redisTemplate;

    public RedisLocalCryptoInfoRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(@Nonnull LocalCryptoInfoEntity aesInfo) throws Exception {
        boolean set = redisTemplate.opsForHash().putIfAbsent(getCacheId(aesInfo.getAppId()), aesInfo.getHeader(), aesInfo);
        if (!set) {
            throw new IllegalStateException("appId, markHeader exist, maybe another instance has init.");
        }
    }

    @Override
    public LocalCryptoInfoEntity get(String appId, String markHeader) throws Exception {
        return (LocalCryptoInfoEntity) redisTemplate.opsForHash().get(getCacheId(appId), markHeader);
    }

    @Override
    @Nonnull
    public List<LocalCryptoInfoEntity> get(String appId) throws Exception {
        List<Object> objects = redisTemplate.opsForHash().values(getCacheId(appId));
        if (CollectionUtils.isEmpty(objects)) {
            return Collections.emptyList();
        }
        List<LocalCryptoInfoEntity> localCryptoInfoEntityList = new LinkedList<>();
        for (Object obj : objects) {
            LocalCryptoInfoEntity aesInfo = (LocalCryptoInfoEntity) obj;
            localCryptoInfoEntityList.add(aesInfo);
        }
        return localCryptoInfoEntityList;
    }


    public String getCacheId(String appId) {
        return KEY_PREFIX + "." + appId;
    }

}
