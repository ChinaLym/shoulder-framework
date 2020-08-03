package org.shoulder.crypto.asymmetric.processor;

import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 执行非对称算法的处理工具。
 * 该接口负责 byte[] 类型加解密，对于 String 的加解密，可以查看 {@link AsymmetricTextCipher}
 *
 * @author lym
 */
public interface AsymmetricCryptoProcessor {

    /**
     * 构建密钥对
     *
     * @param id 密钥对标识
     */
    void buildKeyPair(String id) throws KeyPairException;

    /**
     * 解密（私钥解密）
     *
     * @param id      密钥对标识
     * @param content 密文
     * @return 明文
     */
    byte[] decrypt(String id, byte[] content) throws AsymmetricCryptoException;

    /**
     * 加密（公钥加密）
     *
     * @param id      密钥对标识
     * @param content 明文
     * @return 密文
     */
    byte[] encrypt(String id, byte[] content) throws AsymmetricCryptoException;

    /**
     * 加密（公钥加密）
     *
     * @param content   需加密的数据
     * @param publicKey 对方的公钥
     * @return 密文
     */
    byte[] encrypt(byte[] content, byte[] publicKey) throws AsymmetricCryptoException;

    /**
     * 签名
     *
     * @param content 签名内容
     * @return 签名结果
     */
    byte[] sign(String id, byte[] content) throws AsymmetricCryptoException;

    /**
     * 签名验证
     *
     * @param id        密钥对标识
     * @param content   内容
     * @param signature 签名
     * @return 是否正确
     * @throws AsymmetricCryptoException 验签异常
     */
    boolean verify(String id, byte[] content, byte[] signature) throws AsymmetricCryptoException;

    boolean verify(byte[] publicKey, byte[] content, byte[] signature) throws AsymmetricCryptoException;

    /**
     * 获取公钥
     *
     * @param id 密钥对标识
     * @return 公钥字符串，Base64编码。
     */
    String getPublicKeyString(String id) throws KeyPairException;

    /**
     * 获取公钥
     *
     * @param id 密钥对标识
     * @return 公钥
     */
    PublicKey getPublicKey(String id) throws KeyPairException;

    /**
     * 获取私钥
     *
     * @param id 密钥对标识
     * @return 私钥
     */
    PrivateKey getPrivateKey(String id) throws KeyPairException;

}
