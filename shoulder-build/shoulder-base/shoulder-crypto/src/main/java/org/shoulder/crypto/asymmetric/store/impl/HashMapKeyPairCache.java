package org.shoulder.crypto.asymmetric.store.impl;

import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RSA 密钥对存储-本地存储，适合单机部署场景
 *
 * @author lym
 */
public class HashMapKeyPairCache implements KeyPairCache {

    protected ConcurrentMap<String, KeyPairDto> store = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        LoggerFactory.getLogger(HashMapKeyPairCache.class).debug("LocalKeyPairCache init.");
    }

    @Override
    public void put(String id, @Nonnull KeyPairDto keyPair) {
        store.put(id, keyPair);
    }

    @Override
    public boolean putIfAbsent(String id, @Nonnull KeyPairDto keyPair) {
        return store.putIfAbsent(id, keyPair) == null;
    }

    @Override
    @Nonnull
    public KeyPairDto get(String id) throws NoSuchKeyPairException {
        KeyPairDto keyPair = store.get(id);
        if (keyPair != null) {
            if (keyPair.getExpireTime() != null && Instant.now().isAfter(keyPair.getExpireTime())) {
                store.remove(id);
                throw new NoSuchKeyPairException("keyPair expired, id=" + id);
            }
            return keyPair;
        } else {
            throw new NoSuchKeyPairException("not such keyPair id=" + id);
        }
    }

    /**
     * 销毁，Bean 注销时将密钥对清空
     */
    @PreDestroy
    public void destroy() {
        store.clear();
    }

}
