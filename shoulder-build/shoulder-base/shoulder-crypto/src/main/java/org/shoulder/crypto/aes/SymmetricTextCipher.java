package org.shoulder.crypto.aes;


import org.shoulder.crypto.TextCipher;
import org.shoulder.crypto.exception.CipherRuntimeException;

/**
 * 对称加解密：主要用于本地存储
 *
 * @author lym
 */
public interface SymmetricTextCipher extends TextCipher {

    /**
     * 解密
     *
     * @param cipher 待解密的数据，密文
     * @return 解密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    @Override
    String decrypt(String cipher) throws CipherRuntimeException;

    /**
     * 解密
     *
     * @param cipher 待解密的数据，密文
     * @return 解密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    byte[] decryptAsBytes(String cipher) throws CipherRuntimeException;

    /**
     * 加密（使用自己的默认公钥）
     *
     * @param text 待加密数据
     * @return 加密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    @Override
    String encrypt(String text) throws CipherRuntimeException;

}
