package org.shoulder.crypto.asymmetric.impl;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.nio.charset.Charset;

/**
 * 非对称的加解密以及签名工具实现。
 * 加解密实现为 AsymmetricCryptoProcessor，本类做字符串与 byte[] 的转换。
 * 同时支持默认秘钥，与多秘钥对
 *
 * @author lym
 */
public class DefaultAsymmetricTextCipher implements AsymmetricTextCipher {

    private static final Charset CHAR_SET = ByteSpecification.STD_CHAR_SET;

    private static final Logger log = LoggerFactory.getLogger(DefaultAsymmetricTextCipher.class);

    /**
     * 非对称加密处理器
     */
    private final AsymmetricCryptoProcessor processor;

    /**
     * 秘钥对 id ：用于获取加解密的公钥私钥对。分布式时无需关心其他应用是否会有冲突
     */
    private final String defaultKeyPairId;

    public DefaultAsymmetricTextCipher(AsymmetricCryptoProcessor processor) {
        this(processor, "defaultKeyPairId");
    }

    public DefaultAsymmetricTextCipher(AsymmetricCryptoProcessor processor, String defaultKeyPairId) {
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
    @NonNull
    public String getPublicKey() throws KeyPairException {
        return getPublicKey(defaultKeyPairId);
    }

    @Override
    public String decrypt(String cipher) throws AsymmetricCryptoException {
        return this.decrypt(defaultKeyPairId, cipher);
    }

    @Override
    public String encrypt(String text) throws AsymmetricCryptoException {
        return this.encrypt(defaultKeyPairId, text);
    }

    @Override
    public String sign(String content) throws AsymmetricCryptoException {
        return this.sign(defaultKeyPairId, content);
    }

    @Override
    public boolean verify(String content, String signature) throws AsymmetricCryptoException {
        return this.verify(defaultKeyPairId, content, signature);
    }

    // ================== 多 keyPair =====================

    @Override
    @NonNull
    public String getPublicKey(String keyPairId) throws KeyPairException {
        return processor.getPublicKeyString(this.defaultKeyPairId);
    }

    @Override
    public String decrypt(String keyPairId, String cipher) throws AsymmetricCryptoException {
        if (StringUtils.isNotBlank(cipher)) {
            byte[] text = processor.decrypt(keyPairId, ByteSpecification.decodeToBytes(cipher));
            return new String(text, CHAR_SET);
        }
        return cipher;
    }

    @Override
    public String encrypt(String keyPairId, String text) throws AsymmetricCryptoException {
        if (StringUtils.isNotBlank(text)) {
            byte[] cipher = processor.encrypt(keyPairId, text.getBytes(CHAR_SET));
            return ByteSpecification.encodeToString(cipher);
        }
        return text;
    }

    @Override
    public String sign(String keyPairId, String content) throws AsymmetricCryptoException {
        byte[] signBytes = processor.sign(keyPairId, content.getBytes(CHAR_SET));
        return ByteSpecification.encodeToString(signBytes);
    }

    @Override
    public boolean verify(String keyPairId, String content, String signature) throws AsymmetricCryptoException {
        byte[] signatureBytes = ByteSpecification.decodeToBytes(signature);
        return processor.verify(keyPairId, content.getBytes(CHAR_SET), signatureBytes);
    }

}
