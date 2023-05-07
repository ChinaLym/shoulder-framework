package org.shoulder.autoconfigure.crypto;

import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.cluster.redis.annotation.AppExclusive;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.crypto.asymmetric.AsymmetricCipher;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricCipher;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.CryptoDelegateKeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.HashMapKeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.RedisKeyPairCache;
import org.shoulder.crypto.local.LocalTextCipher;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 非对称加密自动配置
 *
 * @author lym
 */
@AutoConfiguration(after = {AsymmetricCryptoAutoConfiguration.AsymmetricKeyClusterPairCacheConfig.class,
        AsymmetricCryptoAutoConfiguration.AsymmetricKeyClusterPairCacheConfig.class})
@ConditionalOnClass(AsymmetricTextCipher.class)
@ConditionalOnProperty(value = "shoulder.crypto.asymmetric.enable", havingValue = "true", matchIfMissing = true)
public class AsymmetricCryptoAutoConfiguration {

    /**
     * 默认使用 ECC256 完成非对称加密
     */
    @Bean
    @ConditionalOnMissingBean
    public AsymmetricCipher eccAsymmetricProcessor(KeyPairCache keyPairCache) {
        return DefaultAsymmetricCipher.ecc256(keyPairCache);
    }

    /**
     * 非对称加解密 Bean 注入
     *
     * @param asymmetricCipher 基本的非对称加密处理器
     * @return RSA 2048
     */
    @Bean
    @ConditionalOnMissingBean
    public AsymmetricTextCipher asymmetricCrypto(AsymmetricCipher asymmetricCipher) {
        return new DefaultAsymmetricTextCipher(asymmetricCipher);
    }

    /**
     * 默认使用 Hash Map 作为非对称密钥对存储
     */
    @AutoConfiguration
    @ConditionalOnCluster(cluster = false)
    @EnableConfigurationProperties(CryptoProperties.class)
    @ConditionalOnMissingBean(KeyPairCache.class)
    public static class AsymmetricKeyNonClusterPairCacheConfig {
        @Bean
        public KeyPairCache hashMapKeyPairCache(CryptoProperties cryptoProperties) {
            KeyPairCache keyPairCache = new HashMapKeyPairCache();
            // 将配置文件中的预置密钥对加入临时存储
            keyPairCache.put(cryptoProperties.getKeyPair());
            return keyPairCache;
        }
    }

    /**
     * 如果支持集群，则默认使用 redis 作为非对称密钥对存储，并将对应的私钥加密再存至 redis
     */
    @AutoConfiguration
    @ConditionalOnCluster
    @ConditionalOnClass(StringRedisTemplate.class)
    @EnableConfigurationProperties(CryptoProperties.class)
    @ConditionalOnMissingBean(KeyPairCache.class)
    public static class AsymmetricKeyClusterPairCacheConfig {

        @Bean("keyPairCache")
        public KeyPairCache redisKeyPairCache(@AppExclusive StringRedisTemplate redisTemplate,
                                              LocalTextCipher localTextCipher, CryptoProperties cryptoProperties
                , @Autowired(required = false) LocalCryptoInfoRepository localCryptoInfoRepository) {
            if (localCryptoInfoRepository != null && !localCryptoInfoRepository.supportCluster()) {
                // localCrypto 必须也支持集群，否则肯定会报错的
                throw new BaseRuntimeException(CommonErrorCodeEnum.CODING,
                        "localCryptoInfoRepository not support cluster! Current=" + localCryptoInfoRepository.getClass().getName() +
                                "; Consider change another implement, " +
                                "for example: JdbcLocalCryptoInfoRepository、RedisLocalCryptoInfoRepository. " +
                                "you can set [shoulder.crypto.local.repository]=jdbc/redis");
            }
            KeyPairCache keyPairCache = new RedisKeyPairCache(redisTemplate);
            keyPairCache.put(cryptoProperties.getKeyPair());
            LoggerFactory.getLogger(getClass()).debug("redisKeyPairCache provide RedisKeyPairCache.");

            return new CryptoDelegateKeyPairCache(keyPairCache, localTextCipher);
        }

    }


}
