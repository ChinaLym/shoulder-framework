package org.shoulder.crypto.asymmetric.store.impl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.log.ShoulderCryptoLoggers;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RSA 密钥对存储-本地存储，适合单机部署场景
 *
 * @author lym
 */
public class MemoryKeyPairCache implements KeyPairCache {

    protected ConcurrentMap<String, KeyPairDto> store = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        ShoulderCryptoLoggers.DEFAULT.debug("MemoryKeyPairCache init.");
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
