package org.shoulder.autoconfigure.crypto;

import org.shoulder.crypto.symmetric.SymmetricAlgorithmEnum;
import org.shoulder.crypto.symmetric.SymmetricCipher;
import org.shoulder.crypto.symmetric.SymmetricTextCipher;
import org.shoulder.crypto.symmetric.impl.DefaultSymmetricCipher;
import org.shoulder.crypto.symmetric.impl.DefaultSymmetricTextCipher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 非对称加密自动配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SymmetricTextCipher.class)
public class SymmetricCryptoAutoConfiguration {

    /**
     * 默认使用 AES_CBC_PKCS5Padding 完成对称加密
     */
    @Bean
    @ConditionalOnMissingBean
    public SymmetricCipher eccAsymmetricProcessor() {
        return DefaultSymmetricCipher.getFlyweight(SymmetricAlgorithmEnum.AES_CBC_PKCS5Padding.getAlgorithmName());
    }

    /**
     * 默认使用 ECC256 完成非对称加密
     */
    @Bean
    @ConditionalOnMissingBean
    public SymmetricTextCipher eccAsymmetricTextProcessor(SymmetricCipher symmetricCipher) {
        return new DefaultSymmetricTextCipher(symmetricCipher);
    }

}
