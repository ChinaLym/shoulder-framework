package org.shoulder.crypto.asymmetric;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.shoulder.crypto.asymmetric.impl.DefaultAsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.processor.impl.DefaultAsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.store.impl.HashMapKeyPairCache;

/**
 * AsymmetricTextCipherTest
 *
 * @author lym
 */
public class AsymmetricTextCipherTest {

    private DefaultAsymmetricTextCipher textCipher = new DefaultAsymmetricTextCipher(
        DefaultAsymmetricCryptoProcessor.ecc256(new HashMapKeyPairCache())
    );

    /**
     * 测试加解密
     */
    @Test
    public void testCrypt() throws Exception {
        String text = "hello, shoulder";
        String cipher = textCipher.encrypt(text);
        String decrypt = textCipher.decrypt(text);

        Assertions.assertThat(text).isNotEqualTo(cipher);
        Assertions.assertThat(text).isEqualTo(decrypt);
    }

    /**
     * 测试签名和验签
     */
    @Test
    public void testSignAndVerify() throws Exception {
        String text = "hello, shoulder";
        String sign = textCipher.sign(text);

        Assertions.assertThat(textCipher.verify(text, sign)).isTrue();
    }

}
