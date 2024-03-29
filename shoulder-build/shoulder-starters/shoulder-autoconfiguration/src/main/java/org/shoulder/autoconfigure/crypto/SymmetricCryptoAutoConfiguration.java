package org.shoulder.autoconfigure.crypto;

import org.shoulder.crypto.symmetric.SymmetricAlgorithmEnum;
import org.shoulder.crypto.symmetric.SymmetricCipher;
import org.shoulder.crypto.symmetric.SymmetricTextCipher;
import org.shoulder.crypto.symmetric.impl.DefaultSymmetricCipher;
import org.shoulder.crypto.symmetric.impl.DefaultSymmetricTextCipher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 非对称加密自动配置
 *
 * @author lym
 */
@AutoConfiguration
@ConditionalOnClass(SymmetricTextCipher.class)
public class SymmetricCryptoAutoConfiguration {

    public SymmetricCryptoAutoConfiguration() {
        // just for debug
    }

    /**
     * 默认使用 AES_CBC_PKCS5Padding 完成对称加密
     */
    @Bean
    @ConditionalOnMissingBean
    public SymmetricCipher aesCbcSymmetricProcessor() {
        return DefaultSymmetricCipher.getFlyweight(SymmetricAlgorithmEnum.AES_CBC_PKCS5Padding.getAlgorithmName());
    }

    /**
     * 默认使用 ECC256 完成非对称加密
     */
    @Bean
    @ConditionalOnMissingBean
    public SymmetricTextCipher defaultSymmetricTextCipher(SymmetricCipher symmetricCipher) {
        return new DefaultSymmetricTextCipher(symmetricCipher);
    }

}
