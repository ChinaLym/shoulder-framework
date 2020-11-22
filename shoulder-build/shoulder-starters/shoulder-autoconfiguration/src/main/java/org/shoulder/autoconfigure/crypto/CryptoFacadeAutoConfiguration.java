package org.shoulder.autoconfigure.crypto;

import org.shoulder.crypto.CryptoFacade;
import org.shoulder.crypto.DefaultCryptoFacade;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.local.LocalTextCipher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

/**
 * 本地加解密默认配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CryptoFacade.class)
@AutoConfigureAfter(value = {LocalCryptoAutoConfiguration.class, AsymmetricCryptoAutoConfiguration.class})
public class CryptoFacadeAutoConfiguration {


    /**
     * 本地加解密默认实现 Bean 注入
     *
     * @param localCryptoInfoRepository 本地加解密中密钥部件存储
     * @param applicationName           应用唯一标识，不能为空
     * @return 本地加解密 Bean
     */
    @Bean
    @ConditionalOnBean(value = {LocalCryptoAutoConfiguration.class, AsymmetricCryptoAutoConfiguration.class})
    @ConditionalOnMissingBean
    public CryptoFacade cryptoFacade(@Nullable LocalTextCipher local, @Nullable AsymmetricTextCipher asymmetric) {
        return new DefaultCryptoFacade(local, asymmetric);
    }

}
