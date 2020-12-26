package org.shoulder.crypto.asymmetric;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricCipher;
import org.shoulder.crypto.asymmetric.store.impl.HashMapKeyPairCache;

/**
 * 非对称加密测试-框架基础封装
 *
 * @author lym
 */
public class AsymmetricCipherTest {

    private AsymmetricCipher ecc256 = DefaultAsymmetricCipher.ecc256(new HashMapKeyPairCache());

    private AsymmetricCipher rsa2048 = DefaultAsymmetricCipher.rsa2048(new HashMapKeyPairCache());

    /**
     * 测试加解密
     */
    @Test
    public void testCrypt() throws Exception {
        testCrypt(ecc256);
        testCrypt(rsa2048);
    }


    /**
     * 测试签名和验签
     */
    @Test
    public void testSignAndVerify() throws Exception {
        testSign(ecc256);
        testSign(rsa2048);
    }

    private void testCrypt(AsymmetricCipher processor) throws AsymmetricCryptoException {
        String keyPairIndex = StringUtils.uuid32();
        processor.buildKeyPair(keyPairIndex);

        byte[] text = "hello, shoulder".getBytes();
        byte[] encrypted = processor.encrypt(keyPairIndex, text);

        byte[] decrypted = processor.decrypt(keyPairIndex, encrypted);

        Assertions.assertThat(text).isNotEqualTo(encrypted);
        Assertions.assertThat(text).isEqualTo(decrypted);
    }

    private void testSign(AsymmetricCipher processor) throws AsymmetricCryptoException {
        String keyPairIndex = StringUtils.uuid32();
        processor.buildKeyPair(keyPairIndex);

        byte[] text = "hello, shoulder".getBytes();
        byte[] sign = processor.sign(keyPairIndex, text);

        Assertions.assertThat(processor.verify(keyPairIndex, text, sign)).isTrue();
    }

}
