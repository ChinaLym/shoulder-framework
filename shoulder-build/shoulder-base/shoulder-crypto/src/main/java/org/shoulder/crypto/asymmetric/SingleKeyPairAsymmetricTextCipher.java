package org.shoulder.crypto.asymmetric;


import org.shoulder.crypto.TextCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.springframework.lang.NonNull;

/**
 * 非对称加解密：主要用于与前端交互
 * 【简单版】自己只有一个默认秘钥对，每次请求都使用这个一个
 * - 使用简单，容易上手
 * - 资源占用更少、性能更好
 * - 不同用户请求的都是一个 keyPair，这个keyPair一定是热点
 *
 * @author lym
 */
public interface SingleKeyPairAsymmetricTextCipher extends TextCipher {

    /**
     * 获取默认的公钥
     *
     * @return 公钥
     * @throws KeyPairException RsaKeyPairException
     */
    @NonNull
    String getPublicKey() throws KeyPairException;

    /**
     * 解密
     *
     * @param cipher 待解密的数据，密文
     * @return 解密后的
     * @throws AsymmetricCryptoException RsaCryptoException
     */
    @Override
    String decrypt(String cipher) throws AsymmetricCryptoException;

    /**
     * 加密（使用自己的默认公钥）
     *
     * @param text 待加密数据
     * @return 加密后的
     * @throws AsymmetricCryptoException RsaCryptoException
     */
    @Override
    String encrypt(String text) throws AsymmetricCryptoException;

    /**
     * 签名
     *
     * @param content 原始内容，待签名数据
     * @return 签名结果
     * @throws AsymmetricCryptoException 加解密出错
     */
    String sign(String content) throws AsymmetricCryptoException;

    /**
     * 签名验证，常用于校验 content 是否未被篡改
     *
     * @param content   原始内容，待签名数据
     * @param signature 签名
     * @return signature 是否为 content 的签名
     * @throws AsymmetricCryptoException 加解密出错
     */
    boolean verify(String content, String signature) throws AsymmetricCryptoException;
}
