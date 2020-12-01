package org.shoulder.autoconfigure.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.crypto.local.JudgeAbleLocalTextCipher;
import org.shoulder.crypto.local.LocalTextCipher;
import org.shoulder.crypto.local.impl.Aes256LocalTextCipher;
import org.shoulder.crypto.local.impl.LocalTextCipherManager;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.FileLocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.HashMapCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.JdbcLocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.RedisLocalCryptoInfoRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.security.Security;
import java.util.List;

/**
 * 本地加解密默认配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(LocalTextCipher.class)
@ConditionalOnProperty(value = "shoulder.crypto.local.enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CryptoProperties.class)
public class LocalCryptoAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LocalCryptoAutoConfiguration.class);

    // BC
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
            log.debug("Loaded BouncyCastle as crypto provider.");
        }
    }

    /**
     * 本地加解密默认实现 Bean 注入
     *
     * @param localCryptoInfoRepository 本地加解密中密钥部件存储
     * @return 本地加解密 Bean
     */
    @Bean
    @ConditionalOnMissingBean(JudgeAbleLocalTextCipher.class)
    public JudgeAbleLocalTextCipher shoulderLocalCrypto(LocalCryptoInfoRepository localCryptoInfoRepository) {
        return new Aes256LocalTextCipher(localCryptoInfoRepository, AppInfo.appId());
    }

    /**
     * 用户直接使用的接口
     *
     * @param judgeAbleLocalTextCiphers 所有的本地加解密实现（支持多版本共存）
     * @return 本地加解密门面
     */
    @Primary
    @Bean
    public LocalTextCipher localTextCipherManager(@Nullable List<JudgeAbleLocalTextCipher> judgeAbleLocalTextCiphers) {
        return new LocalTextCipherManager(judgeAbleLocalTextCiphers);
    }

    @ConditionalOnMissingBean(LocalCryptoInfoRepository.class)
    @ConditionalOnProperty(name = "shoulder.crypto.local.repository", havingValue = "file", matchIfMissing = true)
    public static class TempFileLocalCryptoInfoRepositoryAutoConfiguration {

        /**
         * 默认使用启动路径下的文件作为密钥部件存储
         *
         * @see LocalCryptoInfoRepository
         */
        @Bean
        public LocalCryptoInfoRepository fileLocalCryptoInfoRepository() {
            log.warn("No LocalCryptoInfoRepository available,  fallback to FileLocalCryptoInfoRepository, " +
                    "storage in your project path, file named {}. " +
                    "Consider create a bean(JdbcLocalCryptoInfoRepository.class) for better security.",
                FileLocalCryptoInfoRepository.DEFAULT_FILE_NAME);

            return new FileLocalCryptoInfoRepository();
        }
    }

    @ConditionalOnCluster(cluster = false)
    @ConditionalOnMissingBean(LocalCryptoInfoRepository.class)
    @AutoConfigureAfter(TempFileLocalCryptoInfoRepositoryAutoConfiguration.class)
    public static class HashMapLocalCryptoInfoRepositoryAutoConfiguration {
        /**
         * 使用了 hashMap，仅供演示。重启后，密钥对将丢失，加密过的数据无法解密！
         * 使用 Error 日志，强烈建议用户更换
         */
        @Bean
        public LocalCryptoInfoRepository hashMapCryptoInfoRepository() {
            // 提示用户该模式只用于演示，不适用于生产，因为加密元信息未持久化，故每次重启都会重新生成。导致加密数据在重启后解密失败
            log.error("You are using memory as LocalCryptoInfoRepository! " +
                "Shoulder strongly recommend that you replace it with other implements " +
                "that can save the IMPORT DATA(root crypto meta data) for a long time");
            return new HashMapCryptoInfoRepository();
        }

    }

    @ConditionalOnMissingBean(LocalCryptoInfoRepository.class)
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnProperty(name = "shoulder.crypto.local.repository", havingValue = "redis")
    public static class RedisLocalCryptoInfoRepositoryAutoConfiguration {
        /**
         * 使用了 redis，通常并不推荐，因为 redis 大多时候是作缓存的，而加密元数据最好要持久化存储
         */
        @Bean
        public LocalCryptoInfoRepository redisLocalCryptoInfoRepository(RedisTemplate<String, Object> redisTemplate) {
            return new RedisLocalCryptoInfoRepository(redisTemplate);
        }
    }

    @ConditionalOnMissingBean(LocalCryptoInfoRepository.class)
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnProperty(name = "shoulder.crypto.local.repository", havingValue = "jdbc")
    public static class JdbcLocalCryptoInfoRepositoryAutoConfiguration {
        /**
         * 使用了 redis，通常并不推荐，因为 redis 大多时候是作缓存的，而加密元数据最好要持久化存储
         */
        @Bean
        public LocalCryptoInfoRepository jdbcLocalCryptoInfoRepository(DataSource dataSource) {
            return new JdbcLocalCryptoInfoRepository(dataSource);
        }
    }


}
