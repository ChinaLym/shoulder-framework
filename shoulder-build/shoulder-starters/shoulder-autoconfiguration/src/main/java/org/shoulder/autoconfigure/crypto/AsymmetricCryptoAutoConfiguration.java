package org.shoulder.autoconfigure.crypto;

import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.cluster.redis.annotation.AppExclusive;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.annotation.Ecc;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.HashMapKeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.RedisKeyPairCache;
import org.shoulder.crypto.local.JudgeAbleLocalTextCipher;
import org.shoulder.crypto.local.LocalTextCipher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 非对称加密自动配置
 *
 * @author lym
 */
@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass(AsymmetricTextCipher.class)
@AutoConfigureAfter(value = {AsymmetricCryptoAutoConfiguration.AsymmetricKeyClusterPairCacheConfig.class,
    AsymmetricCryptoAutoConfiguration.AsymmetricKeyClusterPairCacheConfig.class})
@ConditionalOnProperty(value = "shoulder.crypto.asymmetric.enable", havingValue = "true", matchIfMissing = true)
public class AsymmetricCryptoAutoConfiguration {

    /**
     * 默认使用 ECC256 完成非对称加密
     */
    @Ecc
    @Bean
    @ConditionalOnMissingBean
    public AsymmetricCryptoProcessor eccAsymmetricProcessor(KeyPairCache keyPairCache) {
        return DefaultAsymmetricCryptoProcessor.ecc256(keyPairCache);
    }

    /**
     * 非对称加解密 Bean 注入
     *
     * @param asymmetricCryptoProcessor 基本的非对称加密处理器
     * @return RSA 2048
     */
    @Bean
    @ConditionalOnMissingBean
    public AsymmetricTextCipher asymmetricCrypto(AsymmetricCryptoProcessor asymmetricCryptoProcessor) {
        return new DefaultAsymmetricTextCipher(asymmetricCryptoProcessor);
    }

    /**
     * 默认使用 Hash Map 作为非对称秘钥对存储
     */
    @Configuration(
        proxyBeanMethods = false
    )
    @ConditionalOnCluster(cluster = false)
    @EnableConfigurationProperties(CryptoProperties.class)
    @ConditionalOnMissingBean(KeyPairCache.class)
    public static class AsymmetricKeyNonClusterPairCacheConfig {
        @Bean
        public KeyPairCache hashMapKeyPairCache(CryptoProperties cryptoProperties) {
            KeyPairCache keyPairCache = new HashMapKeyPairCache();
            // 将配置文件中的预置密钥对加入临时存储
            keyPairCache.set(cryptoProperties.getKeyPair());
            return keyPairCache;
        }
    }

    /**
     * 如果支持集群，则默认使用 redis 作为非对称秘钥对存储
     */
    @Configuration(
        proxyBeanMethods = false
    )
    @ConditionalOnCluster
    @ConditionalOnClass(StringRedisTemplate.class)
    @EnableConfigurationProperties(CryptoProperties.class)
    @ConditionalOnMissingBean(KeyPairCache.class)
    public static class AsymmetricKeyClusterPairCacheConfig {

        @Bean("keyPairCache")
        public KeyPairCache redisKeyPairCache(@AppExclusive StringRedisTemplate redisTemplate,
                                              LocalTextCipher localTextCipher, CryptoProperties cryptoProperties) {
            KeyPairCache keyPairCache = new RedisKeyPairCache(redisTemplate, localTextCipher);
            keyPairCache.set(cryptoProperties.getKeyPair());
            LoggerFactory.getLogger(getClass()).debug("redisKeyPairCache provide RedisKeyPairCache.");

            return keyPairCache;
        }

    }


}
