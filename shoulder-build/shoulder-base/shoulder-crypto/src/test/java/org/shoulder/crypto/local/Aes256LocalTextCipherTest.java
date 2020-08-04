package org.shoulder.crypto.local;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.local.impl.Aes256LocalTextCipher;
import org.shoulder.crypto.local.repository.impl.HashMapCryptoInfoRepository;

/**
 * LocalCryptoInfoRepositoryTest
 *
 * @author lym
 */
public class Aes256LocalTextCipherTest {

    private JudgeAbleLocalTextCipher cipher = new Aes256LocalTextCipher(new HashMapCryptoInfoRepository(), "test");


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

        String mockCipherText = Aes256LocalTextCipher.ALGORITHM_HEADER + "asfdghhjkfhwqkdfjwqjp";
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
