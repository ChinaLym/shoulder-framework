package org.shoulder.crypto;


import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.exception.CryptoException;

/**
 * 加解密门面接口，业务代码里可以依赖这个接口
 *
 * @author lym
 */
public interface CryptoFacade {

    // ================================ 本地存储加解密 =====================================

    /**
     * 本地存储加密
     * @param text	待加密数据，不能为null，否则 NPE
     * @return		参数 text 加密后的密文
     * @throws SymmetricCryptoException 加密异常
     */
    String encryptLocal(String text) throws SymmetricCryptoException;

    /**
     * 本地存储加密解密
     * @param cipherText	密文，不能为null，否则 NPE
     * @return				参数 cipherText 解密后的明文
     * @throws SymmetricCryptoException 加密异常
     */
    String decryptLocal(String cipherText) throws SymmetricCryptoException;

    /**
     * 确保本地加密功能正常使用
     * 推荐初始化时调用，可优化第一次加解密性能。
     */
    void initLocal();

    // ================================ RSA 传输加解密、签名验签（如前后交互） =====================================

    /**
     * 获取 RSA 公钥
     * @return 公钥
     * @throws KeyPairException e
     */
    String getPk() throws KeyPairException;

    /**
     * 加密
     * @param text                待加密数据
     * @return 加密后的
     * @throws AsymmetricCryptoException RsaCryptoException
     */
    String encryptAsymmetric(String text) throws AsymmetricCryptoException;


    /**
     * RSA加密（公钥加密）
     * @param text 需加密的数据
     * @param publicKey 对方的公钥
     * @return 密文
     * @throws AsymmetricCryptoException 加解密出错
     */
    String encryptAsymmetric(String text, String publicKey) throws AsymmetricCryptoException;


    /**
     * 解密
     * @param cipherText   	待解密的数据，密文
     * @return 解密后的
     * @throws AsymmetricCryptoException RsaCryptoException
     */
    String decryptAsymmetric(String cipherText) throws AsymmetricCryptoException;

    /**
     * 签名
     * @param text 签名内容
     * @return 签名结果
     * @throws AsymmetricCryptoException 加解密出错
     */
    String sign(String text) throws AsymmetricCryptoException;

    /**
     * 签名验证
     * @param text   内容
     * @param signature 签名
     * @return 是否合法
     * @throws AsymmetricCryptoException 加解密出错
     */
    boolean verify(String text, String signature) throws AsymmetricCryptoException;


    // ================================ 共享存储加解密 =====================================


/*    *//**
     * 加密
     * @param text 要加密的
     * @return  加密后的
     * @throws CryptoException 加密出错
     *//*
    String encryptAuthenticate(String text) throws CryptoException;

    *//**
     * 解密
     * @param cipherText 已加密的
     * @return  解密后的
     * @throws CryptoException 加密出错
     *//*
    String decryptAuthenticate(String cipherText) throws CryptoException;*/

}
