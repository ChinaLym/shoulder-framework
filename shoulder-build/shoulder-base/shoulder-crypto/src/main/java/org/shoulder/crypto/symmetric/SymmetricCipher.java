package org.shoulder.crypto.symmetric;

import org.shoulder.crypto.symmetric.exception.SymmetricCryptoException;

/**
 * 执行对称算法的处理工具。
 * 该接口负责 byte[] 类型加解密，对于 String 的加解密，可以查看 {@link SymmetricTextCipher}
 *
 * @author lym
 */
public interface SymmetricCipher {

    /**
     * 对称加密
     *
     * @param plainText 明文
     * @param key       密钥
     * @param iv        向量
     * @return 密文
     * @throws SymmetricCryptoException 加密失败
     */
    byte[] encrypt(byte[] key, byte[] iv, byte[] plainText) throws SymmetricCryptoException;

    /**
     * 对称解密
     *
     * @param key        密钥
     * @param iv         向量
     * @param cipherText 密文
     * @return 明文
     * @throws SymmetricCryptoException 解密失败
     */
    byte[] decrypt(byte[] key, byte[] iv, byte[] cipherText) throws SymmetricCryptoException;
}
