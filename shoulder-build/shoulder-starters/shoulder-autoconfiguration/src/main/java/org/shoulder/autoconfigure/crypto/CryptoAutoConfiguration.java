package org.shoulder.autoconfigure.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.autoconfigure.redis.RedisAutoConfiguration;
import org.shoulder.cluster.redis.annotation.ApplicationExclusive;
import org.shoulder.crypto.TextCipher;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.annotation.Ecc;
import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.HashMapKeyPairCache;
import org.shoulder.crypto.asymmetric.store.impl.RedisKeyPairCache;
import org.shoulder.crypto.local.JudgeAbleLocalTextCipher;
import org.shoulder.crypto.local.LocalTextCipher;
import org.shoulder.crypto.local.impl.Aes256LocalTextCipher;
import org.shoulder.crypto.local.impl.LocalTextCipherManager;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.JdbcLocalCryptoInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.security.Security;
import java.util.List;
import java.util.Map;

/**
 * 加密默认实现
 * @author lym
 */
@Configuration
@ConditionalOnClass(TextCipher.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(CryptoProperties.class)
public class CryptoAutoConfiguration {

    // BC
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // ************************** 本地加解密 **********************

    /**
     * 默认使用 数据库 作为 localCrypto 的存储
     * @see LocalCryptoInfoRepository
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    public LocalCryptoInfoRepository localCryptoInfoRepository(DataSource dataSource){
        return new JdbcLocalCryptoInfoRepository(dataSource);
    }

    /**
     * 本地加解密 Bean 注入
     * @param localCryptoInfoRepository 本地加解密中秘钥部件存储
     * @param applicationName     应用唯一标识，不能为空
     * @return 本地加解密 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public JudgeAbleLocalTextCipher localCrypto(LocalCryptoInfoRepository localCryptoInfoRepository, @Value("${spring.application.name") String applicationName) {
        return new Aes256LocalTextCipher(localCryptoInfoRepository, applicationName);
    }

    @Bean
    public LocalTextCipher localTextCipherManager(@Nullable List<JudgeAbleLocalTextCipher> judgeAbleLocalTextCiphers){
        return new LocalTextCipherManager(judgeAbleLocalTextCiphers);
    }

    // ************************** 非对称加密 **********************

    @Configuration
    @ConditionalOnMissingBean(KeyPairCache.class)
    @EnableConfigurationProperties(CryptoProperties.class)
    public static class AsymmetricKeyPairCacheConfig {

        @Autowired
        private CryptoProperties cryptoProperties;

        /**
         * 如果支持集群，则默认使用 redis 作为非对称秘钥对存储
         */
        @Bean("redisKeyPairCache")
        @ConditionalOnCluster
        @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
        public KeyPairCache redisKeyPairCache(@ApplicationExclusive StringRedisTemplate redisTemplate, LocalTextCipher localTextCipher){
            KeyPairCache keyPairCache = new RedisKeyPairCache(redisTemplate, localTextCipher);
            return addPropertyKeyPairs(keyPairCache);
        }
        /**
         * 默认使用 Hash Map 作为非对称秘钥对存储
         */
        @Bean("hashMapKeyPairCache")
        @ConditionalOnCluster(cluster = false)
        public KeyPairCache hashMapKeyPairCache(){
            KeyPairCache keyPairCache = new HashMapKeyPairCache();
            return addPropertyKeyPairs(keyPairCache);
        }

        /**
         * 将配置文件中的预置密钥对加入临时存储
         */
        private KeyPairCache addPropertyKeyPairs(KeyPairCache keyPairCache){
            Map<String, KeyPairDto> keyPairDtoMap = cryptoProperties.getKeyPair();
            if(keyPairDtoMap == null || keyPairDtoMap.isEmpty()){
                return keyPairCache;
            }
            keyPairDtoMap.forEach(keyPairCache::set);
            return keyPairCache;
        }

    }


    /**
     * 默认使用 ECC256 完成非对称加密
     */
    @Ecc
    @Bean
    @ConditionalOnMissingBean
    public DefaultAsymmetricCryptoProcessor eccAsymmetricProcessor(KeyPairCache keyPairCache){
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


}
