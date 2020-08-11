package org.shoulder.autoconfigure.crypto;

import lombok.extern.shoulder.SLog;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.crypto.local.JudgeAbleLocalTextCipher;
import org.shoulder.crypto.local.LocalTextCipher;
import org.shoulder.crypto.local.impl.Aes256LocalTextCipher;
import org.shoulder.crypto.local.impl.LocalTextCipherManager;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.HashMapCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.JdbcLocalCryptoInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.security.Security;
import java.util.List;

/**
 * 本地加解密默认配置
 *
 * @author lym
 */
@SLog
@Configuration
@ConditionalOnClass(LocalTextCipher.class)
@ConditionalOnProperty(value = "shoulder.crypto.local.enable", havingValue = "true", matchIfMissing = true)
//@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(CryptoProperties.class)
public class LocalCryptoAutoConfiguration {

    // BC
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
            log.debug("Loaded BouncyCastle as crypto provider.");
        }
    }

    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(LocalCryptoInfoRepository.class)
    @AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration.Tomcat",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration.Hikari",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration.Dbcp2",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration.Generic",
    })
    @ConditionalOnProperty(name = "shoulder.crypto.local.repository", havingValue = "jdbc", matchIfMissing = true)
    public static class JdbcLocalCryptoInfoRepositoryAutoConfiguration {

        /**
         * 默认使用 数据库 作为 localCrypto 的秘钥部件存储
         * todo default use file?
         *
         * @see LocalCryptoInfoRepository
         */
        @Bean
        public LocalCryptoInfoRepository jdbcLocalCryptoInfoRepository(DataSource dataSource) {
            log.debug("No LocalCryptoInfoRepository available, try fall back to use JdbcLocalCryptoInfoRepository");
            return new JdbcLocalCryptoInfoRepository(dataSource);
        }
    }

    @ConditionalOnMissingBean(LocalCryptoInfoRepository.class)
    @AutoConfigureAfter(JdbcLocalCryptoInfoRepositoryAutoConfiguration.class)
    public static class HashMapLocalCryptoInfoRepositoryAutoConfiguration {
        /**
         * 使用了 hashMap，仅供演示。重启后，密钥对将丢失，加密过的数据无法解密！
         * 警告用户必须更换
         */
        @Bean
        public LocalCryptoInfoRepository hashMapCryptoInfoRepository(){
            log.warn("You are using memory as LocalCryptoInfoRepository! " +
                "Shoulder strongly recommend that you replace it with other implements " +
                "that can save the IMPORT DATA(root crypto meta data) for a long time");
            return new HashMapCryptoInfoRepository();
        }

    }

    /**
     * 本地加解密默认实现 Bean 注入
     *
     * @param localCryptoInfoRepository 本地加解密中秘钥部件存储
     * @param applicationName           应用唯一标识，不能为空
     * @return 本地加解密 Bean
     */
    @Bean
    @ConditionalOnMissingBean(JudgeAbleLocalTextCipher.class)
    public JudgeAbleLocalTextCipher shoulderLocalCrypto(LocalCryptoInfoRepository localCryptoInfoRepository, @Value("${spring.application.name") String applicationName) {
        return new Aes256LocalTextCipher(localCryptoInfoRepository, applicationName);
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


}
