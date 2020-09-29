package org.shoulder.crypto.aes;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.shoulder.crypto.aes.exception.AesCryptoException;

import java.nio.charset.StandardCharsets;
public class AesUtilTest {

    /**
     * 测试 aes
     */
    @Test
    public void testAesByteArray() throws Exception {

        byte[] text = "testAes".getBytes(StandardCharsets.UTF_8);

        byte[] iv = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
            1, 2, 3, 4, 5, 6};

        byte[] key = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6};

        // 解密后的加密过的明文
        byte[] processedData = AesUtil.decrypt(AesUtil.encrypt(text, key, iv), key, iv);
        Assertions.assertThat(text).isEqualTo(processedData);
    }

    /**
     * 测试 aes 的参数校验
     * 秘钥长度不符合标准 aes 的要求（必须为16/24/32）
     */
    @Test(expected = AesCryptoException.class)
    public void testAesKeyVerify() throws Exception {

        byte[] text = "testAes".getBytes(StandardCharsets.UTF_8);

        byte[] iv = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
            1, 2, 3, 4, 5, 6};

        byte[] key = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        // 解密后的加密过的明文
        byte[] processedData = AesUtil.decrypt(AesUtil.encrypt(text, key, iv), key, iv);
        Assertions.assertThat(text).isEqualTo(processedData);
    }

    /**
     * 测试 aes 的参数校验
     * 向量长度不符合标准 aes 的要求（必须为16）
     */
    @Test(expected = AesCryptoException.class)
    public void testAesIvVerify() throws Exception {

        byte[] text = "testAes".getBytes(StandardCharsets.UTF_8);

        byte[] iv = {1, 2, 3, 4, 5, 6};

        byte[] key = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            1, 2, 3, 4, 5, 6};

        // 解密后的加密过的明文
        byte[] processedData = AesUtil.decrypt(AesUtil.encrypt(text, key, iv), key, iv);
        Assertions.assertThat(text).isEqualTo(processedData);
    }

}
