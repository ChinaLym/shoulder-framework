package org.shoulder.crypto.asymmetric.impl;

import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.crypto.asymmetric.AsymmetricCipher;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.exception.CipherRuntimeException;
import org.shoulder.crypto.exception.CryptoErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * 非对称的加解密以及签名工具实现。
 * 加解密实现为 AsymmetricCryptoProcessor，本类做字符串与 byte[] 的转换。
 * 同时支持默认密钥，与多密钥对
 *
 * @author lym
 */
public class DefaultAsymmetricTextCipher implements AsymmetricTextCipher {

    private static final Charset CHAR_SET = ByteSpecification.STD_CHAR_SET;

    private static final Logger log = LoggerFactory.getLogger(DefaultAsymmetricTextCipher.class);

    /**
     * 非对称加密处理器
     */
    private final AsymmetricCipher processor;

    /**
     * 密钥对 id ：用于获取加解密的公钥私钥对。分布式时无需关心其他应用是否会有冲突
     */
    private final String defaultKeyPairId;

    public DefaultAsymmetricTextCipher(AsymmetricCipher processor) {
        this(processor, "defaultKeyPairId");
    }

    public DefaultAsymmetricTextCipher(AsymmetricCipher processor, String defaultKeyPairId) {
        this.defaultKeyPairId = defaultKeyPairId;
        this.processor = processor;
        try {
            this.processor.buildKeyPair(defaultKeyPairId);
        } catch (Exception e) {
            log.error("asymmetric crypto init fail");
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nonnull
    public String getPublicKey() {
        return getPublicKey(defaultKeyPairId);
    }

    @Override
    public String decrypt(String cipher) throws CipherRuntimeException {
        return this.decrypt(defaultKeyPairId, cipher);
    }

    @Override
    public byte[] decryptAsBytes(String cipher) throws CipherRuntimeException {
        return this.decryptAsBytes(defaultKeyPairId, cipher);
    }

    @Override
    public String encrypt(String text) throws CipherRuntimeException {
        return this.encrypt(defaultKeyPairId, text);
    }

    @Override
    public String sign(String content) throws CipherRuntimeException {
        return this.sign(defaultKeyPairId, content);
    }

    @Override
    public boolean verify(String content, String signature) throws CipherRuntimeException {
        return this.verify(defaultKeyPairId, content, signature);
    }

    @Override
    public boolean verify(byte[] content, byte[] signature) throws CipherRuntimeException {
        return this.verify(defaultKeyPairId, content, signature);
    }

    // ================== 多 keyPair =====================

    @Override
    @Nonnull
    public String getPublicKey(String keyPairId) throws CipherRuntimeException {
        try {
            return processor.getPublicKeyString(this.defaultKeyPairId);
        } catch (KeyPairException e) {
            throw CryptoErrorCodeEnum.NO_SUCH_KEY_PAIR.toException(e);
        }
    }

    @Override
    public String decrypt(String keyPairId, String cipher) throws CipherRuntimeException {
        if (StringUtils.isNotBlank(cipher)) {
            return new String(decryptAsBytes(keyPairId, cipher), CHAR_SET);
        }
        return "";
    }

    @Override
    public byte[] decryptAsBytes(String keyPairId, String cipher) throws CipherRuntimeException {
        if (StringUtils.isBlank(cipher)) {
            return new byte[0];
        }
        try {
            return processor.decrypt(keyPairId, ByteSpecification.decodeToBytes(cipher));
        } catch (AsymmetricCryptoException e) {
            throw CryptoErrorCodeEnum.DECRYPT_FAIL.toException(e);
        }
    }

    @Override
    public String encrypt(String keyPairId, String text) throws CipherRuntimeException {
        if (StringUtils.isNotBlank(text)) {
            try {
                byte[] cipher = processor.encrypt(keyPairId, text.getBytes(CHAR_SET));
                return ByteSpecification.encodeToString(cipher);
            } catch (AsymmetricCryptoException e) {
                throw CryptoErrorCodeEnum.ENCRYPT_FAIL.toException(e);
            }
        }
        return text;
    }

    @Override
    public String sign(String keyPairId, String content) throws CipherRuntimeException {
        try {
            byte[] signBytes = processor.sign(keyPairId, content.getBytes(CHAR_SET));
            return ByteSpecification.encodeToString(signBytes);
        } catch (AsymmetricCryptoException e) {
            throw CryptoErrorCodeEnum.SIGN_FAIL.toException(e);
        }
    }

    @Override
    public boolean verify(String keyPairId, String content, String signature) throws CipherRuntimeException {
        byte[] signatureBytes = ByteSpecification.decodeToBytes(signature);
        try {
            return processor.verify(keyPairId, content.getBytes(CHAR_SET), signatureBytes);
        } catch (AsymmetricCryptoException e) {
            throw CryptoErrorCodeEnum.SIGN_VERIFY_FAIL.toException(e);
        }
    }

    @Override
    public boolean verify(String keyPairId, byte[] content, byte[] signature) throws CipherRuntimeException {
        try {
            return processor.verify(keyPairId, content, signature);
        } catch (AsymmetricCryptoException e) {
            throw CryptoErrorCodeEnum.SIGN_VERIFY_FAIL.toException(e);
        }
    }

}
