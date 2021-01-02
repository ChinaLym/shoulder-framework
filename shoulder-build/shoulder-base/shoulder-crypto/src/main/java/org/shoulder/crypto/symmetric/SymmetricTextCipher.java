package org.shoulder.crypto.symmetric;


import org.shoulder.crypto.symmetric.exception.SymmetricCryptoException;

/**
 * 对称加解密：主要用于本地存储
 *
 * @author lym
 */
public interface SymmetricTextCipher {


    /**
     * 加密
     *
     * @param text 待加密数据
     * @return 加密后的
     * @throws SymmetricCryptoException AesCryptoException
     */
    byte[] encryptAsBytes(byte[] key, byte[] iv, String text) throws SymmetricCryptoException;

    /**
     * 加密（使用自己的默认公钥）
     *
     * @param text 待加密数据
     * @return 加密后的
     * @throws SymmetricCryptoException AesCryptoException
     */
    String encrypt(byte[] key, byte[] iv, String text) throws SymmetricCryptoException;

    /**
     * 解密
     *
     * @param cipher 待解密的数据，密文
     * @return 解密后的
     * @throws SymmetricCryptoException AesCryptoException
     */
    byte[] decryptAsBytes(byte[] key, byte[] iv, String cipher) throws SymmetricCryptoException;

    /**
     * 解密
     *
     * @param cipher 待解密的数据，密文
     * @return 解密后的
     * @throws SymmetricCryptoException AesCryptoException
     */
    String decrypt(byte[] key, byte[] iv, String cipher) throws SymmetricCryptoException;


}
