package org.shoulder.crypto.asymmetric.store.impl;

import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.local.LocalTextCipher;

import javax.annotation.Nonnull;

/**
 * RSA 密钥对存储-Redis 存储，适合应用支持集群部署的场景
 * 如果不使用过期时间等redis特有操作，可以通过双层缓存优化访问速度
 *
 * @author lym
 */
public class CryptoDelegateKeyPairCache implements KeyPairCache {

    /**
     * 注意维护应用隔离
     */
    private KeyPairCache delegate;

    /**
     * 局部加密器，保证即使多个应用共享一个 redis，也无法获得其他应用的私钥信息
     * - 仅加密私钥：性能高；【推荐方案】
     * - 加密整个密钥对：性能差、可能导致已知明文攻击（需要localTextCipher的算法能避免该攻击）
     * - 极端情况无法检测密钥对被外界篡改（破坏者能直接或间接使用localTextCipher时）
     */
    private final LocalTextCipher localTextCipher;


    public CryptoDelegateKeyPairCache(KeyPairCache delegate, LocalTextCipher localTextCipher) {
        this.delegate = delegate;
        this.localTextCipher = localTextCipher;
    }

    @Override
    public void put(String id, @Nonnull KeyPairDto keyPairDto) {
        // 为了安全和性能，仅将私钥加密，避免已知明文攻击
        delegate.put(id, encryptKeyPair(keyPairDto));
    }

    @Override
    public boolean putIfAbsent(String id, @Nonnull KeyPairDto keyPairDto) {
        return delegate.putIfAbsent(id, encryptKeyPair(keyPairDto));
    }

    @Nonnull
    @Override
    public KeyPairDto get(String id) throws NoSuchKeyPairException {
        return decryptKeyPair(delegate.get(id));
    }

    private KeyPairDto encryptKeyPair(KeyPairDto keyPairDto) {
        String cipherVk = localTextCipher.encrypt(keyPairDto.getVk());
        keyPairDto.setVk(cipherVk);
        return keyPairDto;
    }

    private KeyPairDto decryptKeyPair(KeyPairDto keyPairDto) {
        String cipherVk = localTextCipher.decrypt(keyPairDto.getVk());
        keyPairDto.setVk(cipherVk);
        return keyPairDto;
    }

}
