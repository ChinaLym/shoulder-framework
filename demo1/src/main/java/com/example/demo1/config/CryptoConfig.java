package com.example.demo1.config;

import com.example.demo1.controller.crypto.asymmetric.EccCryptoDemoController;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricCipher;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.local.repository.impl.FileLocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.HashMapCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.JdbcLocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.RedisLocalCryptoInfoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CryptoConfig 加密相关配置
 *
 * @author lym
 */
@Configuration
public class CryptoConfig {

    /**
     * fixme 这里使用了用于demo测试的 HashMapCryptoInfoRepository（每次运行将重置！）
     * 生产环境需要配置为可持久化的，如使用 mysql 作为持久化！！！！！
     *
     * @see JdbcLocalCryptoInfoRepository
     * @see RedisLocalCryptoInfoRepository
     * @see FileLocalCryptoInfoRepository
     */
    @Bean
    public HashMapCryptoInfoRepository hashMapCryptoInfoRepository() {
        return new HashMapCryptoInfoRepository();
    }


    /**
     * 打开 @Bean 注释，非对称加解密将使用 RSA 算法
     * shoulder 默认使用性能更好，安全系数更高的 ECC 算法，{@link EccCryptoDemoController}
     * <p>
     * 默认 RSA 算法参数详见 {@link DefaultAsymmetricCipher#rsa2048}
     */
    //@Bean
    public DefaultAsymmetricCipher rsa2048(KeyPairCache keyPairCache) {
        return DefaultAsymmetricCipher.rsa2048(keyPairCache);
    }

    /**
     * 使用 redis 作为存储
     */
    /*@Bean("keyPairCache")
    public KeyPairCache redisKeyPairCache(StringRedisTemplate redisTemplate,
                                          LocalTextCipher localTextCipher, CryptoProperties cryptoProperties) {
        KeyPairCache keyPairCache = new RedisKeyPairCache(redisTemplate, localTextCipher);
        keyPairCache.set(cryptoProperties.getKeyPair());
        LoggerFactory.getLogger(getClass()).debug("redisKeyPairCache provide RedisKeyPairCache.");

        return keyPairCache;
    }*/

}
