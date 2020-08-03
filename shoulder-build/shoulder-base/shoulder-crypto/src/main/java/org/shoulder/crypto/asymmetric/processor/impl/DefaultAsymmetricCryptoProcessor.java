package org.shoulder.crypto.asymmetric.processor.impl;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.crypto.asymmetric.dto.KeyPairDto;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.exception.NoSuchKeyPairException;
import org.shoulder.crypto.asymmetric.factory.AsymmetricKeyPairFactory;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.shoulder.crypto.asymmetric.store.KeyPairCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 非对称加解密工具实现
 *
 * @author lym
 */
public class DefaultAsymmetricCryptoProcessor implements AsymmetricCryptoProcessor, ByteSpecification {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAsymmetricCryptoProcessor.class);

    private final AsymmetricKeyPairFactory keyPairFactory;

    private final KeyPairCache keyPairCache;

    protected Lock lock = new ReentrantLock();

    /**
     * 算法名称
     */
    private final String algorithm;

    /**
     * 算法实现提供商
     */
    private final String provider;

    /**
     * 秘钥位数
     */
    private final int keyLength;

    /**
     * 算法实现
     */
    private final String transformation;

    /**
     * 签名算法
     */
    private final String signatureAlgorithm;

    @PreDestroy
    public void destroy() {
        keyPairCache.destroy();
    }

    public DefaultAsymmetricCryptoProcessor(String algorithm, int keyLength, String transformation, String signatureAlgorithm,
                                            String provider, KeyPairCache keyPairCache) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.keyLength = keyLength;
        this.signatureAlgorithm = signatureAlgorithm;
        this.transformation = transformation;
        this.keyPairCache = keyPairCache;
        this.keyPairFactory = new AsymmetricKeyPairFactory(algorithm, keyLength, provider);
    }

    @Override
    public void buildKeyPair(String id) throws KeyPairException {
        KeyPair kp = null;
        lock.lock();
        try {
            kp = getKeyPairFromDto(keyPairCache.get(id));
            keyPairCache.set(id, new KeyPairDto(kp));
        } catch (NoSuchKeyPairException e) {
            kp = keyPairFactory.build();
            keyPairCache.set(id, new KeyPairDto(kp));
        } finally {
            lock.unlock();
        }
    }

    private KeyPair getKeyPairFromDto(KeyPairDto dto) throws KeyPairException {
        return keyPairFactory.buildFrom(
            ByteSpecification.decodeToBytes(dto.getPk()),
            ByteSpecification.decodeToBytes(dto.getVk())
        );
    }

    @Override
    public byte[] decrypt(String id, byte[] content) throws AsymmetricCryptoException {
        try {
            Cipher cipher = Cipher.getInstance(transformation, provider);
            cipher.init(Cipher.DECRYPT_MODE, keyPairFactory.generatePrivateKey(getKeyPairFromDto(keyPairCache.get(id)).getPrivate().getEncoded()));
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | KeyPairException | NoSuchProviderException e) {
            throw new AsymmetricCryptoException("decrypt fail.", e);
        }
    }

    @Override
    public byte[] encrypt(String id, byte[] content) throws AsymmetricCryptoException {
        try {
            // 对数据加密
            Cipher cipher = Cipher.getInstance(transformation, provider);
            cipher.init(Cipher.ENCRYPT_MODE, keyPairFactory.generatePublicKey(getKeyPairFromDto(keyPairCache.get(id)).getPublic().getEncoded()));
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | KeyPairException | NoSuchProviderException e) {
            throw new AsymmetricCryptoException("encrypt fail.", e);
        }
    }

    @Override
    public byte[] encrypt(byte[] content, byte[] publicKey) throws AsymmetricCryptoException {
        try {
            // 对数据加密
            Cipher cipher = Cipher.getInstance(transformation, provider);
            cipher.init(Cipher.ENCRYPT_MODE, keyPairFactory.generatePublicKey(publicKey));
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | KeyPairException | NoSuchProviderException e) {
            throw new AsymmetricCryptoException("encrypt fail.", e);
        }
    }

    @Override
    public byte[] sign(String id, byte[] content) throws AsymmetricCryptoException {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(keyPairFactory.generatePrivateKey(getKeyPairFromDto(keyPairCache.get(id)).getPrivate().getEncoded()));
            signature.update(content);
            return signature.sign();
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | KeyPairException e) {
            throw new AsymmetricCryptoException("sign fail.", e);
        }
    }

    @Override
    public boolean verify(String id, byte[] content, byte[] signature) throws AsymmetricCryptoException {
        try {
            return this.verify(getKeyPairFromDto(keyPairCache.get(id)).getPublic().getEncoded(), content, signature);
        } catch (NoSuchKeyPairException e) {
            throw new AsymmetricCryptoException("verify fail.", e);
        }
    }

    @Override
    public boolean verify(byte[] publicKey, byte[] content, byte[] signature) throws AsymmetricCryptoException {
        try {
            Signature sign = Signature.getInstance(signatureAlgorithm);
            sign.initVerify(keyPairFactory.generatePublicKey(publicKey));
            sign.update(content);
            return sign.verify(signature);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | KeyPairException e) {
            throw new AsymmetricCryptoException("verify fail.", e);
        }
    }

    @Override
    public PublicKey getPublicKey(String id) throws KeyPairException {
        return getKeyPairFromDto(keyPairCache.get(id)).getPublic();
    }

    @Override
    public String getPublicKeyString(String id) throws KeyPairException {
        return ByteSpecification.encodeToString(getPublicKey(id).getEncoded());
    }

    @Override
    public PrivateKey getPrivateKey(String id) throws KeyPairException {
        return getKeyPairFromDto(keyPairCache.get(id)).getPrivate();
    }

    // ------------------ 提供两个常用的加密方案 ------------------

    public static DefaultAsymmetricCryptoProcessor ecc256(KeyPairCache keyPairCache) {
        return new DefaultAsymmetricCryptoProcessor(
            "EC",
            256,
            "ECIES",
            "SHA256withECDSA",
            BouncyCastleProvider.PROVIDER_NAME,
            keyPairCache
        );

    }

    public static DefaultAsymmetricCryptoProcessor rsa2048(KeyPairCache keyPairCache) {
        return new DefaultAsymmetricCryptoProcessor(
            "RSA",
            2048,
            "RSA/ECB/PKCS1Padding",
            "SHA256WithRSA",
            BouncyCastleProvider.PROVIDER_NAME,
            keyPairCache
        );
    }

}
