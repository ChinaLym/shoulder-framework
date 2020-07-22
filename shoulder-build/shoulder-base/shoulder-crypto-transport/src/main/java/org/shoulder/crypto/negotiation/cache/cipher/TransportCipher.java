package org.shoulder.crypto.negotiation.cache.cipher;

import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.negotiation.cache.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;

/**
 * 传输加解密，使用者使用
 * @author lym
 */
public class TransportCipher {

    private KeyExchangeResult keyExchangeResult;

    /** 数据密钥明文 */
    private byte[] dk;


    private TransportCipher(KeyExchangeResult keyExchangeInfo, byte[] dk) {
        this.keyExchangeResult = keyExchangeInfo;
        this.dk = dk;
    }

    /**
     * 创建加密专用
     * @param keyExchangeInfo 密钥协商结果信息
     * @param dk 数据密钥明文
     * @return 解密器
     */
    public static TransportCipher decryptor(KeyExchangeResult keyExchangeInfo, byte[] dk) {
        return new Decryptor(keyExchangeInfo, dk);
    }

    /**
     * 创建加密专用
     * @param keyExchangeInfo 密钥协商结果信息
     * @return 加密器
     */
    public static Encryptor encryptor(KeyExchangeResult keyExchangeInfo, byte[] dk){
        return new Encryptor(keyExchangeInfo, dk);
    }


    /**
     * 加密
     * @param toCipher 待加密明文
     * @return  密文
     * @throws AesCryptoException 加密异常
     */
    public String encrypt(String toCipher) throws AesCryptoException {
        return TransportCryptoUtil.encrypt(keyExchangeResult, dk, toCipher);
    }

    /**
     * 解密
     * @param cipherText 对方加密过的密文
     * @return 明文
     * @throws AesCryptoException 加密异常
     */
    public String decrypt(String cipherText) throws AesCryptoException {
        return TransportCryptoUtil.decrypt(keyExchangeResult, dk, cipherText);
    }


    // ---------------------------------------------------------------------------------------------

    /**
     * 加密器
     */
    private static class Encryptor extends TransportCipher {

        private Encryptor(KeyExchangeResult keyExchangeInfo, byte[] dk) {
            super(keyExchangeInfo, dk);
        }

        @Override
        public String decrypt(String toCipher) {
            throw new UnsupportedOperationException("Can't decrypt with a encryptor!");
        }

    }


    /**
     * 解密器
     */
    private static class Decryptor extends TransportCipher {

        private Decryptor(KeyExchangeResult keyExchangeInfo, byte[] dk) {
            super(keyExchangeInfo, dk);
        }

        @Override
        public String encrypt(String toCipher) {
            throw new UnsupportedOperationException("Can't encrypt with a decryptor!");
        }

    }


}
