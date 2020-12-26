package org.shoulder.crypto.aes.impl;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.crypto.aes.SymmetricCipher;
import org.shoulder.crypto.aes.SymmetricTextCipher;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;

import java.nio.charset.Charset;

/**
 * 对称的加解密以及签名工具实现。
 * 加解密实现为 SymmetricCryptoProcessor，本类做字符串与 byte[] 的转换。
 *
 * @author lym
 */
public class DefaultSymmetricTextCipher implements SymmetricTextCipher {

    private static final Logger log = LoggerFactory.getLogger(DefaultSymmetricTextCipher.class);

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
