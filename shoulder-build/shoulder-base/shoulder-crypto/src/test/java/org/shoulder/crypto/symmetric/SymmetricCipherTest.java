package org.shoulder.crypto.symmetric;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.crypto.symmetric.exception.SymmetricCryptoException;
import org.shoulder.crypto.symmetric.impl.DefaultSymmetricCipher;
import org.shoulder.crypto.symmetric.impl.DefaultSymmetricTextCipher;

/**
 * 非对称加密测试-框架基础封装
 *
 * @author lym
 */
public class SymmetricCipherTest {

    private final SymmetricCipher aes = DefaultSymmetricCipher.getFlyweight(SymmetricAlgorithmEnum.AES_CBC_PKCS5Padding.getAlgorithmName());

    private final SymmetricCipher sm4 = DefaultSymmetricCipher.getFlyweight(SymmetricAlgorithmEnum.AES_GCM.getAlgorithmName());

    private final DefaultSymmetricTextCipher aesT = new DefaultSymmetricTextCipher(aes);

    private final DefaultSymmetricTextCipher sm4T = new DefaultSymmetricTextCipher(sm4);

    /**
     * 测试加解密
     */
    @Test
    public void testCrypt() throws Exception {
        testByteCrypt(aes);
        testByteCrypt(sm4);
        testTextCrypt(aesT);
        testTextCrypt(sm4T);
    }

    private void testByteCrypt(SymmetricCipher cipher) throws SymmetricCryptoException {
        byte[] key = ByteUtils.randomBytes(16);
        byte[] iv = ByteUtils.randomBytes(16);
        byte[] text = "hello, shoulder".getBytes();
        byte[] encrypted = cipher.encrypt(key, iv, text);
        byte[] decrypted = cipher.decrypt(key, iv, encrypted);

        Assertions.assertNotEquals(text, encrypted);
        Assertions.assertArrayEquals(text, decrypted);
    }

    private void testTextCrypt(DefaultSymmetricTextCipher textCipher) throws SymmetricCryptoException {
        byte[] key = ByteUtils.randomBytes(16);
        byte[] iv = ByteUtils.randomBytes(16);
        String text = "hello, shoulder";
//        byte[] encryptedBytes = textCipher.encryptAsBytes(key, iv, text);
        String encryptedStr = textCipher.encrypt(key, iv, text);

        byte[] decryptedBytes = textCipher.decryptAsBytes(key, iv, encryptedStr);
        String decryptedStr = textCipher.decrypt(key, iv, encryptedStr);

        Assertions.assertEquals(text, decryptedStr);
        Assertions.assertArrayEquals(text.getBytes(), decryptedBytes);
    }

}
