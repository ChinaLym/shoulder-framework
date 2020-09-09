package org.shoulder.crypto.asymmetric.store.impl;

import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.local.LocalTextCipher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

/**
 * RSA 秘钥存储-Redis 存储，适合应用支持集群部署的场景
 * 如果不使用过期时间等redis特有操作，可以通过双层缓存优化访问速度
 *
 * @author lym
 */
public class RedisKeyPairCache implements KeyPairCache {

    private RedisTemplate<String, String> redisTemplate;

    private String keyPrefix;

    /**
     * 局部加密器，保证即使多个应用共享一个 redis，也无法获得其他应用的私钥信息
     * - 仅加密私钥：性能高；【推荐方案】
     * - 加密整个密钥对：性能差、可能导致已知明文攻击（需要localTextCipher的算法能避免该攻击）
     * - 极端情况无法检测密钥对被外界篡改（破坏者能直接或间接使用localTextCipher时）
     */
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
            // 为了安全和性能，仅将私钥加密，避免已知明文攻击
            String kpJson = JsonUtils.toJson(keyPairDto);
            String encryptKp = localTextCipher.encrypt(kpJson);

            redisTemplate.opsForValue().setIfAbsent(key, encryptKp);
        } catch (SymmetricCryptoException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public KeyPairDto get(String id) throws NoSuchKeyPairException {
        String key = addRedisPrefix(id);
        String cipherKp = redisTemplate.opsForValue().get(key);
        if (cipherKp != null) {
            try {
                String kp = localTextCipher.decrypt(cipherKp);
                KeyPairDto keyPairDto = JsonUtils.toObject(kp, KeyPairDto.class);
                if (StringUtils.isEmpty(keyPairDto.getVk())) {
                    // 数据完整性遭到外界恶意破坏，不应继续使用
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

	private void encryptKeyPair(KeyPairDto keyPairDto) throws SymmetricCryptoException {
		String cipherVk = localTextCipher.encrypt(keyPairDto.getVk());
		keyPairDto.setVk(cipherVk);
	}

	private void decryptKeyPair(KeyPairDto keyPairDto) throws SymmetricCryptoException {
		String cipherVk = localTextCipher.decrypt(keyPairDto.getVk());
		keyPairDto.setVk(cipherVk);
	}

    @Override
    public void destroy() {

    }

}
