package org.shoulder.crypto.asymmetric;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.crypto.aes.DefaultSymmetricCryptoProcessor;
import org.shoulder.crypto.aes.SymmetricCryptoProcessor;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;

/**
 * 非对称加密测试-框架基础封装
 *
 * @author lym
 */
public class SymmetricCipherTest {

    private SymmetricCryptoProcessor aes = DefaultSymmetricCryptoProcessor.aes_256_CBC_PKCS5Padding();

    private SymmetricCryptoProcessor sm4 = DefaultSymmetricCryptoProcessor.sm4_256_CBC_PKCS5Padding();

    /**
     * 测试加解密
     */
    @Test
    public void testCrypt() throws Exception {
        testCrypt(aes);
        testCrypt(sm4);
    }


    private void testCrypt(SymmetricCryptoProcessor processor) throws SymmetricCryptoException {
        byte[] key = ByteUtils.randomBytes(16);
        byte[] iv = ByteUtils.randomBytes(16);
        byte[] text = "hello, shoulder".getBytes();
        byte[] encrypted = processor.encrypt(key, iv, text);
        byte[] decrypted = processor.decrypt(key, iv, encrypted);

        Assertions.assertThat(text).isNotEqualTo(encrypted);
        Assertions.assertThat(text).isEqualTo(decrypted);
    }

}
