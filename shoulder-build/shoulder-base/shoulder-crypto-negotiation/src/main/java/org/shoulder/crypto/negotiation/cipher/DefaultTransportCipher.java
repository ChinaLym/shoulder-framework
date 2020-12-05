package org.shoulder.crypto.negotiation.cipher;

import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.exception.CryptoErrorCodeEnum;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.util.TransportCryptoUtil;

/**
 * 传输加解密，使用者使用
 *
 * @author lym
 */
public class DefaultTransportCipher implements TransportTextCipher {

    private NegotiationResult negotiationResult;

    /**
     * 数据密钥明文
     */
    private byte[] dk;


    private DefaultTransportCipher(NegotiationResult keyExchangeInfo, byte[] dk) {
        this.negotiationResult = keyExchangeInfo;
        this.dk = dk;
    }

    /**
     * 创建解密器
     *
     * @param keyExchangeInfo 密钥协商结果信息
     * @param dk              数据密钥明文
     * @return 解密器
     */
    public static DecryptCipher buildDecryptCipher(NegotiationResult keyExchangeInfo, byte[] dk) {
        return new DecryptCipher(keyExchangeInfo, dk);
    }

    /**
     * 创建加密器
     *
     * @param keyExchangeInfo 密钥协商结果信息
     * @return 加密器
     */
    public static EncryptCipher buildEncryptCipher(NegotiationResult keyExchangeInfo, byte[] dk) {
        return new EncryptCipher(keyExchangeInfo, dk);
    }


    /**
     * 加密
     *
     * @param toCipher 待加密明文
     * @return 密文
     */
    @Override
    public String encrypt(String toCipher) {
        try {
            return TransportCryptoUtil.encrypt(negotiationResult, dk, toCipher);
        } catch (AesCryptoException e) {
            throw CryptoErrorCodeEnum.ENCRYPT_FAIL.toException(e);
        }
    }

    /**
     * 解密
     *
     * @param cipherText 对方加密过的密文
     * @return 明文
     */
    @Override
    public String decrypt(String cipherText) {
        try {
            return TransportCryptoUtil.decrypt(negotiationResult, dk, cipherText);
        } catch (AesCryptoException e) {
            throw CryptoErrorCodeEnum.ENCRYPT_FAIL.toException(e);
        }
    }

    /**
     * 加密或解密，根据实现类具体职责决定
     */
    @Override
    public String doCipher(String input) {
        throw new UnsupportedOperationException("not support!");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * 加密器
     */
    private static class EncryptCipher extends DefaultTransportCipher {

        private EncryptCipher(NegotiationResult keyExchangeInfo, byte[] dk) {
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
        public String doCipher(String input) {
            return super.encrypt(input);
        }
    }


    /**
     * 解密器
     */
    private static class DecryptCipher extends DefaultTransportCipher {

        private DecryptCipher(NegotiationResult keyExchangeInfo, byte[] dk) {
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
        public String doCipher(String input) {
            return super.decrypt(input);
        }
    }


}
