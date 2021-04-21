package org.shoulder.crypto.symmetric;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.crypto.symmetric.exception.SymmetricCryptoException;
import org.shoulder.crypto.symmetric.impl.DefaultSymmetricCipher;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Aes 对称加密测试
 *
 * @author lym
 */
public class SymmetricCryptoUtilsTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private SymmetricCipher symmetricCipher = DefaultSymmetricCipher.getFlyweight(
            SymmetricAlgorithmEnum.AES_CBC_PKCS5Padding.getAlgorithmName()
    );

    /**
     * 测试 aes
     */
    @Test
    public void testAesByteArray() throws Exception {

        byte[] text = "testAes".getBytes(UTF8);

        byte[] iv = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6};

        byte[] key = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                1, 2, 3, 4, 5, 6,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                1, 2, 3, 4, 5, 6};

        // 解密后的加密过的明文
        byte[] processedData = symmetricCipher.decrypt(key, iv, symmetricCipher.encrypt(key, iv, text));
        Assertions.assertThat(text).isEqualTo(processedData);
    }

    /**
     * 测试 aes 的参数校验
     * 密钥长度不符合标准 aes 的要求（必须为16/24/32）
     */
    @Test
    public void testAesKeyVerify() throws Exception {

        byte[] text = "testAes".getBytes(UTF8);

        byte[] iv = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6};
        // key length 不对
        byte[] key = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                1, 2, 3, 4, 5, 6,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        SymmetricCryptoException ex = assertThrows(SymmetricCryptoException.class, () -> symmetricCipher.encrypt(key, iv, text));
        assertAll(
                () -> assertEquals("symmetricCryptoException doCipher(mode=1) Exception!", ex.getMessage()),
                () -> assertEquals(IllegalArgumentException.class, ex.getCause().getClass()),
                () -> assertEquals("the length of parameter 'key' not support, only support [16, 24, 32]", ex.getCause().getMessage())
        );

    }

    /**
     * 测试 aes 的参数校验
     * 向量长度不符合标准 aes 的要求（必须为16）
     */
    @Test
    public void testAesIvVerify() throws Exception {

        byte[] text = "testAes".getBytes(UTF8);
        // iv length 不对
        byte[] iv = {1, 2, 3, 4, 5, 6};

        byte[] key = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                1, 2, 3, 4, 5, 6,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                1, 2, 3, 4, 5, 6};

        SymmetricCryptoException ex = assertThrows(SymmetricCryptoException.class, () -> symmetricCipher.encrypt(key, iv, text));
        assertAll(
                () -> assertEquals("symmetricCryptoException doCipher(mode=1) Exception!", ex.getMessage()),
                () -> assertEquals(IllegalArgumentException.class, ex.getCause().getClass()),
                () -> assertEquals("the parameter 'iv' must be 128 bit(16 byte)", ex.getCause().getMessage())
        );
    }

}
