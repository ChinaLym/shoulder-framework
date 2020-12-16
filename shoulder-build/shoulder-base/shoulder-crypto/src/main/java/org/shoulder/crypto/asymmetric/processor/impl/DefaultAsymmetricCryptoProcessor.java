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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.time.Duration;
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
    /**
     * 算法名称
     */
    private final String algorithm;
    /**
     * 算法实现提供商
     */
    private final String provider;
    /**
     * 密钥位数
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

    protected Lock lock = new ReentrantLock();

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

    // ------------------ 提供两个推荐使用的安全加密方案 ------------------

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


    /**
     * 注意，椭圆曲线算法的签名速度远快于 RSA2048，近10倍，但验签速度慢了有10-20倍，因此通常对服务器更友好，对客户端要求更高
     * 安全更高、存储更少（密钥更短）
     */
    public static DefaultAsymmetricCryptoProcessor ecc256(KeyPairCache keyPairCache) {
        return new DefaultAsymmetricCryptoProcessor(
            "EC",
            256,
            "ECDH",
            "SHA256withECDSA",
            BouncyCastleProvider.PROVIDER_NAME,
            keyPairCache
        );
    }

    // sm2 暂不稳定，api 不统一，暂不纳入默认支持
    /*public static DefaultAsymmetricCryptoProcessor sm2(KeyPairCache keyPairCache) {
        return new DefaultAsymmetricCryptoProcessor(
            "SM2",
            256,
            "SM2",
            "SM3withSM2",
            BouncyCastleProvider.PROVIDER_NAME,
            keyPairCache
        );
    }*/


    @Override
    public synchronized void buildKeyPair(String id, Duration ttl) throws KeyPairException {
        try {
            // 如果能成功将缓存中的值拿出来构建密钥对，则说明已经构建过，无需再次构建
            getKeyPair(id);
        } catch (NoSuchKeyPairException e) {
            // 未拿到或构建出错，则重新生成并保存
            keyPairCache.put(id, new KeyPairDto(keyPairFactory.build(), ttl));
            // 此时若构建密钥对失败则表示不支持使用者设置的加密算法，需要使用者检查自己的配置，因此直接将异常抛出
        }
    }

    @Override
    public void buildKeyPair(String id) throws KeyPairException {
        this.buildKeyPair(id, null);
    }

    private KeyPair getKeyPairFromDto(KeyPairDto dto) throws KeyPairException {
        KeyPair keyPair = null;
        if ((keyPair = dto.getOriginKeyPair()) != null) {
            // 内存存储，无需反序列化
            return keyPair;
        }
        keyPair = keyPairFactory.buildFrom(
            ByteSpecification.decodeToBytes(dto.getPk()),
            ByteSpecification.decodeToBytes(dto.getVk())
        );
        // 兼容非直接存储的本地缓存，是否存在这种？
        dto.setOriginKeyPair(keyPair);
        return keyPair;
    }

    @Override
    public byte[] decrypt(String id, byte[] content) throws AsymmetricCryptoException {
        try {
            Cipher cipher = Cipher.getInstance(transformation, provider);
            cipher.init(Cipher.DECRYPT_MODE, keyPairFactory.generatePrivateKey(getKeyPair(id).getPrivate().getEncoded()));
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
            cipher.init(Cipher.ENCRYPT_MODE, keyPairFactory.generatePublicKey(getKeyPair(id).getPublic().getEncoded()));
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
            signature.initSign(keyPairFactory.generatePrivateKey(getKeyPair(id).getPrivate().getEncoded()));
            signature.update(content);
            return signature.sign();
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | KeyPairException e) {
            throw new AsymmetricCryptoException("sign fail.", e);
        }
    }

    @Override
    public boolean verify(String id, byte[] content, byte[] signature) throws AsymmetricCryptoException {
        try {
            return this.verify(getKeyPair(id).getPublic().getEncoded(), content, signature);
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
    public KeyPair getKeyPair(String id) throws KeyPairException {
        return getKeyPairFromDto(keyPairCache.get(id));
    }

    @Override
    public PublicKey getPublicKey(String id) throws KeyPairException {
        return getKeyPair(id).getPublic();
    }


    @Override
    public String getPublicKeyString(String id) throws KeyPairException {
        return keyPairCache.get(id).getPk();
    }

    @Override
    public PrivateKey getPrivateKey(String id) throws KeyPairException {
        return getKeyPair(id).getPrivate();
    }

}
