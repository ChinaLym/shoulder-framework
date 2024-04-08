package org.shoulder.crypto.symmetric.impl;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.log.Logger;
import org.shoulder.crypto.log.ShoulderCryptoLoggers;
import org.shoulder.crypto.symmetric.SymmetricCipher;
import org.shoulder.crypto.symmetric.SymmetricTextCipher;
import org.shoulder.crypto.symmetric.exception.SymmetricCryptoException;

import java.nio.charset.Charset;

/**
 * 对称的加解密以及签名工具实现。
 * 加解密实现为 SymmetricCryptoProcessor，本类做字符串与 byte[] 的转换。
 *
 * @author lym
 */
public class DefaultSymmetricTextCipher implements SymmetricTextCipher {

    private static final Logger log = ShoulderCryptoLoggers.DEFAULT;

    private static final Charset CHAR_SET = ByteSpecification.STD_CHAR_SET;

    /**
     * 对称加密器
     */
    private final SymmetricCipher processor;

    public DefaultSymmetricTextCipher(SymmetricCipher processor) {
        this.processor = processor;
    }


    @Override
    public byte[] encryptAsBytes(byte[] key, byte[] iv, String text) throws SymmetricCryptoException {
        return processor.encrypt(key, iv, text.getBytes(CHAR_SET));
    }

    @Override
    public String encrypt(byte[] key, byte[] iv, String text) throws SymmetricCryptoException {
        return ByteSpecification.encodeToString(encryptAsBytes(key, iv, text));
    }


    @Override
    public String decrypt(byte[] key, byte[] iv, String cipher) throws SymmetricCryptoException {
        return new String(decryptAsBytes(key, iv, cipher), CHAR_SET);
    }

    @Override
    public byte[] decryptAsBytes(byte[] key, byte[] iv, String cipher) throws SymmetricCryptoException {
        return processor.decrypt(key, iv, ByteSpecification.decodeToBytes(cipher));
    }


}
