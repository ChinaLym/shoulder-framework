package org.shoulder.crypto.asymmetric;


import org.shoulder.crypto.TextCipher;
import org.shoulder.crypto.exception.CipherRuntimeException;
import org.springframework.lang.NonNull;

/**
 * 非对称加解密：主要用于与前端交互
 * 【简单版】自己只有一个默认密钥对，每次请求都使用这个一个
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
     * @throws CipherRuntimeException RsaKeyPairException
     */
    @NonNull
    String getPublicKey() throws CipherRuntimeException;

    /**
     * 解密
     *
     * @param cipher 待解密的数据，密文
     * @return 解密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    @Override
    String decrypt(String cipher) throws CipherRuntimeException;

    /**
     * 解密
     *
     * @param cipher 待解密的数据，密文
     * @return 解密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    byte[] decryptAsBytes(String cipher) throws CipherRuntimeException;

    /**
     * 加密（使用自己的默认公钥）
     *
     * @param text 待加密数据
     * @return 加密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    @Override
    String encrypt(String text) throws CipherRuntimeException;

    /**
     * 签名
     *
     * @param content 原始内容，待签名数据
     * @return 签名结果
     * @throws CipherRuntimeException 加解密出错
     */
    String sign(String content) throws CipherRuntimeException;

    /**
     * 签名验证，常用于校验 content 是否未被篡改
     *
     * @param content   原始内容，待签名数据
     * @param signature 签名
     * @return signature 是否为 content 的签名
     * @throws CipherRuntimeException 加解密出错
     */
    boolean verify(String content, String signature) throws CipherRuntimeException;

    /**
     * 使用 keyPairId 对应密钥对的私钥【验证签名】，常用于校验 content 是否未被篡改
     *
     * @param content   原始内容，待签名数据
     * @param signature 签名
     * @return signature 是否为 content 的签名
     * @throws CipherRuntimeException 加解密出错
     */
    boolean verify(byte[] content, byte[] signature) throws CipherRuntimeException;
}
