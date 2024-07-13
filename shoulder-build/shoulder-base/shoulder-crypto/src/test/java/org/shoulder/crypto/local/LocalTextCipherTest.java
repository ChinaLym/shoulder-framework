package org.shoulder.crypto.local;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.crypto.local.impl.DefaultLocalTextCipher;
import org.shoulder.crypto.local.impl.LocalTextCipherManager;
import org.shoulder.crypto.local.repository.impl.MemoryCryptoInfoRepository;

import java.security.InvalidParameterException;

/**
 * 本地存储加解密测试
 *
 * @author lym
 */
public class LocalTextCipherTest {

    private static final JudgeAbleLocalTextCipher cipher = new DefaultLocalTextCipher(new MemoryCryptoInfoRepository(), "test");

    private static final LocalTextCipher localTextCipher = new LocalTextCipherManager(cipher);

    @BeforeAll
    public static void initTest() {
        localTextCipher.ensureInit();
    }

    /**
     * 测试本地加解密
     */
    @Test
    public void testCrypto() {
        // 非法密文不予处理
        Assertions.assertThrows(InvalidParameterException.class, () -> localTextCipher.decrypt("random_invalid_input"));

        String text = "hello shoulder";
        // 加密
        String cipherText = localTextCipher.encrypt(text);
        // 解密
        String decryptedText = localTextCipher.decrypt(cipherText);
        Assertions.assertEquals(text, decryptedText);


        // 加密
        Assertions.assertThrows(BaseRuntimeException.class, () -> localTextCipher.decrypt(""));

    }

}
