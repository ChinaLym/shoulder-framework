package org.shoulder.crypto.asymmetric.factory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 密钥对工厂，为 AsymmetricProcessor 服务
 *
 * @author lym
 */
public class AsymmetricKeyPairFactory {

    // BC
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }


    /**
     * 加密算法
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
     * 初始化需要告知
     * 父类算法名、提供商、秘钥长度
     *
     * @param algorithm 算法名，如 RSA
     * @param keyLength 秘钥长度如 256
     * @param provider  提供商，如 BC
     */
    public AsymmetricKeyPairFactory(String algorithm, int keyLength, String provider) {
        this.algorithm = algorithm;
        this.provider = provider;
        this.keyLength = keyLength;
    }


    public KeyPair build() throws KeyPairException {
        try {
            KeyPairGenerator keyPairGen = provider == null ? KeyPairGenerator.getInstance(algorithm) : KeyPairGenerator.getInstance(algorithm, provider);
            // 初始化密钥对生成器
            keyPairGen.initialize(keyLength, new SecureRandom());
            // 生成一个密钥对，保存在keyPair中
            return keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new KeyPairException("build key pair error.", e);
        }
    }

    /**
     * @implSpec 通过子类 generatePublicKey、generatePublicKey 方法创建密钥对
     */
    public KeyPair buildFrom(byte[] publicKey, byte[] privateKey) throws KeyPairException {
        PublicKey pk = generatePublicKey(publicKey);
        PrivateKey vk = generatePrivateKey(privateKey);
        return new KeyPair(pk, vk);
    }

    public PublicKey generatePublicKey(byte[] keyEncode) throws KeyPairException {
        try {
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyEncode);
            KeyFactory keyFactory = provider == null ? KeyFactory.getInstance(algorithm) : KeyFactory.getInstance(algorithm, provider);
            return keyFactory.generatePublic(x509KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            throw new KeyPairException("generate public key error.", e);
        }
    }

    public PrivateKey generatePrivateKey(byte[] keyEncode) throws KeyPairException {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyEncode);
            KeyFactory keyFactory = provider == null ? KeyFactory.getInstance(algorithm) : KeyFactory.getInstance(algorithm, provider);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            throw new KeyPairException("generate private key error.", e);
        }
    }

}
