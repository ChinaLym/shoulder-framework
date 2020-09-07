package org.shoulder.crypto.asymmetric.store.impl;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.local.LocalTextCipher;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * RSA 秘钥存储-Redis 存储，适合集群部署场景
 *
 * @author lym
 */

public class RedisKeyPairCache implements KeyPairCache {

    private RedisTemplate<String, String> redisTemplate;

    private String keyPrefix;

    private LocalTextCipher localTextCipher;

    public RedisKeyPairCache(StringRedisTemplate redisTemplate, LocalTextCipher localTextCipher) {
        this(redisTemplate, localTextCipher, "crypto-asymmetric:");
    }

    public RedisKeyPairCache(StringRedisTemplate redisTemplate, LocalTextCipher localTextCipher, String keyPrefix) {
        this.keyPrefix = keyPrefix;
        this.redisTemplate = redisTemplate;
        this.localTextCipher = localTextCipher;
    }

    @Override
    public void set(String id, @NonNull KeyPairDto keyPairDto) {
        String key = addRedisPrefix(id);
        try {
            String kpJson = JsonUtils.toJson(keyPairDto);
            String encryptKp = localTextCipher.encrypt(kpJson);

            redisTemplate.opsForValue().setIfAbsent(key, encryptKp);
        } catch (SymmetricCryptoException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public KeyPairDto get(String id) throws NoSuchKeyPairException {
        String key = addRedisPrefix(id);
        String cipherKp = redisTemplate.opsForValue().get(key);
        if (cipherKp != null) {
            try {
                String kp = localTextCipher.decrypt(cipherKp);
                KeyPairDto keyPairDto = JsonUtils.toObject(kp, KeyPairDto.class);
                if (StringUtils.isEmpty(keyPairDto.getVk())) {
                    throw new NoSuchKeyPairException("KeyPair.privateKey is empty, id= " + id);
                }
                return keyPairDto;
            } catch (SymmetricCryptoException e) {
                throw new NoSuchKeyPairException("can't decrypt keyPair id= " + id);
            }
        } else {
            throw new NoSuchKeyPairException("can't found keyPair id= " + id);
        }
    }

    private String addRedisPrefix(String id) {
        return keyPrefix + id;
    }


    @PostConstruct
    public void init() {
        LoggerFactory.getLogger(RedisKeyPairCache.class).debug("RedisKeyPairCache init.");
    }

/*
	private void encryptKeyPair(KeyPairDto keyPairDto) throws SymmetricCryptoException {
		String cipherVk = localCrypto.encrypt(keyPairDto.getVk());
		keyPairDto.setVk(cipherVk);
	}

	private void decryptKeyPair(KeyPairDto keyPairDto) throws SymmetricCryptoException {
		String cipherVk = localCrypto.decrypt(keyPairDto.getVk());
		keyPairDto.setVk(cipherVk);
	}*/

    @Override
    public void destroy() {

    }

}
