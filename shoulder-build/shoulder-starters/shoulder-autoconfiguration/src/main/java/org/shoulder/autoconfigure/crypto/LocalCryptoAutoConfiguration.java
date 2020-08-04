package org.shoulder.autoconfigure.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.crypto.local.JudgeAbleLocalTextCipher;
import org.shoulder.crypto.local.LocalTextCipher;
import org.shoulder.crypto.local.impl.Aes256LocalTextCipher;
import org.shoulder.crypto.local.impl.LocalTextCipherManager;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.shoulder.crypto.local.repository.impl.JdbcLocalCryptoInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@Configuration
@ConditionalOnClass(LocalTextCipher.class)
//@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(CryptoProperties.class)
public class LocalCryptoAutoConfiguration {

    // BC
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 默认使用 数据库 作为 localCrypto 的秘钥部件存储
     * todo 更改为默认使用内存 或 file 并警告用户必须更换？
     *
     * @see LocalCryptoInfoRepository
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    public LocalCryptoInfoRepository localCryptoInfoRepository(DataSource dataSource) {
        return new JdbcLocalCryptoInfoRepository(dataSource);
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
