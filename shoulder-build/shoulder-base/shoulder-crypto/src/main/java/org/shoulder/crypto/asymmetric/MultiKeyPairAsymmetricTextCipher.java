package org.shoulder.crypto.asymmetric;


import org.shoulder.crypto.TextCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.springframework.lang.NonNull;

/**
 * 非对称加解密：主要用于与前端交互
 * 【多个 keyPair版本】
 * - 占用更多的存储
 * - 无法充分利用操作系统缓存
 * - 相对更安全
 *
 * @author lym
 */
public interface MultiKeyPairAsymmetricTextCipher extends TextCipher {

    // ======================== 指定 keyPairId ======================

    /**
     * 获取 keyPairId 对应秘钥对的公钥部分，若不存在则会新建
     *
     * @param keyPairId 秘钥对标识
     * @return 公钥
     * @throws KeyPairException RsaKeyPairException
     */
    @NonNull
    String getPublicKey(String keyPairId) throws KeyPairException;

    /**
     * 使用 keyPairId 对应秘钥对的私钥【解密】cipher
     *
     * @param keyPairId 秘钥对标识
     * @param cipher    待解密的数据，密文
     * @return 解密后的
     * @throws AsymmetricCryptoException RsaCryptoException
     */
    String decrypt(String keyPairId, String cipher) throws AsymmetricCryptoException;

    /**
     * 解密
     *
     * @param keyPairId 秘钥对标识
     * @param cipher    待解密的数据，密文
     * @return 解密后的
     * @throws AsymmetricCryptoException RsaCryptoException
     */
    byte[] decryptAsBytes(String keyPairId, String cipher) throws AsymmetricCryptoException;

    /**
     * 使用 keyPairId 对应秘钥对的私钥【签名】
     *
     * @param keyPairId 秘钥对标识
     * @param content   原始内容，待签名数据
     * @return 签名结果
     * @throws AsymmetricCryptoException 加解密出错
     */
    String sign(String keyPairId, String content) throws AsymmetricCryptoException;

    /**
     * 使用 keyPairId 对应秘钥对的私钥【验证签名】，常用于校验 content 是否未被篡改
     *
     * @param keyPairId 秘钥对标识
     * @param content   原始内容，待签名数据
     * @param signature 签名
     * @return signature 是否为 content 的签名
     * @throws AsymmetricCryptoException 加解密出错
     */
    boolean verify(String keyPairId, String content, String signature) throws AsymmetricCryptoException;

    /**
     * 使用 keyPairId 对应秘钥对的私钥【验证签名】，常用于校验 content 是否未被篡改
     *
     * @param keyPairId 秘钥对标识
     * @param content   原始内容，待签名数据
     * @param signature 签名
     * @return signature 是否为 content 的签名
     * @throws AsymmetricCryptoException 加解密出错
     */
    boolean verify(String keyPairId, byte[] content, byte[] signature) throws AsymmetricCryptoException;

}
