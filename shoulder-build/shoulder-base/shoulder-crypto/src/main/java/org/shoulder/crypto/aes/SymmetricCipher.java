package org.shoulder.crypto.aes;

import org.shoulder.crypto.aes.exception.SymmetricCryptoException;

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
     * @param content 明文
     * @param key     密钥
     * @param iv      向量
     * @throws SymmetricCryptoException 加密失败
     * @return 密文
     */
    byte[] encrypt(byte[] key, byte[] iv, byte[] content) throws SymmetricCryptoException;

    /**
     * 对称解密
     *
     * @param key        密钥
     * @param iv         向量
     * @param cipherText 密文
     * @throws SymmetricCryptoException 解密失败
     * @return 明文
     */
    byte[] decrypt(byte[] key, byte[] iv, byte[] cipherText) throws SymmetricCryptoException;
}
