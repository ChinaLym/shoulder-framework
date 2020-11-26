package org.shoulder.autoconfigure.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;

import java.io.FileNotFoundException;
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
     * @param applicationName           应用唯一标识，不能为空
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
        public LocalCryptoInfoRepository fileLocalCryptoInfoRepository() throws FileNotFoundException {
            log.warn("No LocalCryptoInfoRepository available,  fallback to FileLocalCryptoInfoRepository, " +
                    "storage in your project path, file named {}. " +
                    "Consider create a bean(JdbcLocalCryptoInfoRepository.class) for better security.",
                FileLocalCryptoInfoRepository.DEFAULT_FILE_NAME);

            return new FileLocalCryptoInfoRepository();
        }
    }

    @ConditionalOnMissingBean(LocalCryptoInfoRepository.class)
    @AutoConfigureAfter(TempFileLocalCryptoInfoRepositoryAutoConfiguration.class)
    public static class HashMapLocalCryptoInfoRepositoryAutoConfiguration {
        /**
         * 使用了 hashMap，仅供演示。重启后，密钥对将丢失，加密过的数据无法解密！
         * 警告用户必须更换
         */
        @Bean
        public LocalCryptoInfoRepository hashMapCryptoInfoRepository() {
            log.warn("You are using memory as LocalCryptoInfoRepository! " +
                "Shoulder strongly recommend that you replace it with other implements " +
                "that can save the IMPORT DATA(root crypto meta data) for a long time");
            return new HashMapCryptoInfoRepository();
        }

    }


}
