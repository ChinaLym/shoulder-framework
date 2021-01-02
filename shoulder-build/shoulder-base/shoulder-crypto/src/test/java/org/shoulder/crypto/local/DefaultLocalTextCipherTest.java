package org.shoulder.crypto.local;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.shoulder.crypto.local.impl.DefaultLocalTextCipher;
import org.shoulder.crypto.local.repository.impl.HashMapCryptoInfoRepository;
import org.shoulder.crypto.symmetric.exception.SymmetricCryptoException;

/**
 * 本地存储加解密测试
 *
 * @author lym
 */
public class DefaultLocalTextCipherTest {

    private JudgeAbleLocalTextCipher cipher = new DefaultLocalTextCipher(new HashMapCryptoInfoRepository(), "test");


    /**
     * 测试密文识别
     */
    @Test
    public void testJudgeAble1() {
        String fakerCipherText = "asfdghhjkfhwqkdfjwqjp";
        boolean support = cipher.support(fakerCipherText);
        Assertions.assertThat(support).isFalse();
    }

    /**
     * 测试密文识别
     */
    @Test
    public void testJudgeAble2() {

        String mockCipherText = DefaultLocalTextCipher.ALGORITHM_HEADER + "asfdghhjkfhwqkdfjwqjp";
        boolean support = cipher.support(mockCipherText);
        Assertions.assertThat(support).isTrue();
    }

    /**
     * 测试密文识别
     */
    @Test
    public void testEncrypt() throws SymmetricCryptoException {
        String text = "hello, shoulder";
        String cipherText = cipher.encrypt(text);
        String decryptText = cipher.decrypt(cipherText);

        Assertions.assertThat(text).isNotEqualTo(cipherText);
        Assertions.assertThat(text).isEqualTo(decryptText);
    }

}
