package org.shoulder.crypto;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.crypto.exception.CryptoException;

/**
 * String 文本加密器
 * @author lym
 */
public interface TextCipher extends ByteSpecification {

    /**
     * 加密
     * @param text 明文
     * @return  加密后的密文
     * @throws CryptoException 加解密错误
     */
    String encrypt(String text) throws CryptoException;

    /**
     *  解密
     * @param cipher    密文
     * @return          解密后的明文
     * @throws CryptoException 加解密错误
     */
    String decrypt(String cipher) throws CryptoException;

}
