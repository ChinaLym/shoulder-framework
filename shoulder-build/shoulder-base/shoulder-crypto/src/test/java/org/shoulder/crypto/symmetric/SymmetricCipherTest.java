package org.shoulder.crypto.symmetric;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.crypto.symmetric.exception.SymmetricCryptoException;
import org.shoulder.crypto.symmetric.impl.DefaultSymmetricCipher;

/**
 * 非对称加密测试-框架基础封装
 *
 * @author lym
 */
public class SymmetricCipherTest {

    private SymmetricCipher aes = DefaultSymmetricCipher.getFlyweight(SymmetricAlgorithmEnum.AES_CBC_PKCS5Padding.getAlgorithmName());

    private SymmetricCipher sm4 = DefaultSymmetricCipher.getFlyweight(SymmetricAlgorithmEnum.AES_GCM.getAlgorithmName());

    /**
     * 测试加解密
     */
    @Test
    public void testCrypt() throws Exception {
        testCrypt(aes);
        testCrypt(sm4);
    }


    private void testCrypt(SymmetricCipher processor) throws SymmetricCryptoException {
        byte[] key = ByteUtils.randomBytes(16);
        byte[] iv = ByteUtils.randomBytes(16);
        byte[] text = "hello, shoulder".getBytes();
        byte[] encrypted = processor.encrypt(key, iv, text);
        byte[] decrypted = processor.decrypt(key, iv, encrypted);

        Assertions.assertThat(text).isNotEqualTo(encrypted);
        Assertions.assertThat(text).isEqualTo(decrypted);
    }

}
