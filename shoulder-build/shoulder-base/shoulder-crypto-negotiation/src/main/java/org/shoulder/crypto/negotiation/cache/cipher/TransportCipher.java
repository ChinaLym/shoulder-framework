package org.shoulder.crypto.negotiation.cache.cipher;

import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.negotiation.cache.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;

/**
 * 传输加解密，使用者使用
 *
 * @author lym
 */
public class TransportCipher {

    private KeyExchangeResult keyExchangeResult;

    /**
     * 数据密钥明文
     */
    private byte[] dk;


    private TransportCipher(KeyExchangeResult keyExchangeInfo, byte[] dk) {
        this.keyExchangeResult = keyExchangeInfo;
        this.dk = dk;
    }

    /**
     * 创建解密器
     *
     * @param keyExchangeInfo 密钥协商结果信息
     * @param dk              数据密钥明文
     * @return 解密器
     */
    public static DecryptCipher buildDecryptCipher(KeyExchangeResult keyExchangeInfo, byte[] dk) {
        return new DecryptCipher(keyExchangeInfo, dk);
    }

    /**
     * 创建加密器
     *
     * @param keyExchangeInfo 密钥协商结果信息
     * @return 加密器
     */
    public static EncryptCipher buildEncryptCipher(KeyExchangeResult keyExchangeInfo, byte[] dk) {
        return new EncryptCipher(keyExchangeInfo, dk);
    }


    /**
     * 加密
     *
     * @param toCipher 待加密明文
     * @return 密文
     * @throws AesCryptoException 加密异常
     */
    public String encrypt(String toCipher) throws AesCryptoException {
        return TransportCryptoUtil.encrypt(keyExchangeResult, dk, toCipher);
    }

    /**
     * 解密
     *
     * @param cipherText 对方加密过的密文
     * @return 明文
     * @throws AesCryptoException 加密异常
     */
    public String decrypt(String cipherText) throws AesCryptoException {
        return TransportCryptoUtil.decrypt(keyExchangeResult, dk, cipherText);
    }

    /**
     * 加密或解密，根据实现类具体职责决定
     */
    public String doCipher(String input) throws AesCryptoException {
        throw new UnsupportedOperationException("not support!");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * 加密器
     */
    private static class EncryptCipher extends TransportCipher {

        private EncryptCipher(KeyExchangeResult keyExchangeInfo, byte[] dk) {
            super(keyExchangeInfo, dk);
        }

        @Override
        public String decrypt(String toCipher) {
            throw new UnsupportedOperationException("Can't decrypt with a encryptCipher!");
        }

        /**
         * 加密
         */
        @Override
        public String doCipher(String input) throws AesCryptoException {
            return super.encrypt(input);
        }
    }


    /**
     * 解密器
     */
    private static class DecryptCipher extends TransportCipher {

        private DecryptCipher(KeyExchangeResult keyExchangeInfo, byte[] dk) {
            super(keyExchangeInfo, dk);
        }

        @Override
        public String encrypt(String toCipher) {
            throw new UnsupportedOperationException("Can't encrypt with a decryptCipher!");
        }

        /**
         * 加密
         */
        @Override
        public String doCipher(String input) throws AesCryptoException {
            return super.decrypt(input);
        }
    }


}
