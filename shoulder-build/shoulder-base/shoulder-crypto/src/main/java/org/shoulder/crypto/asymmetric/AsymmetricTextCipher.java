package org.shoulder.crypto.asymmetric;


import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;

/**
 * 非对称加解密门面接口
 *
 * @author lym
 */
public interface AsymmetricTextCipher extends SingleKeyPairAsymmetricTextCipher, MultiKeyPairAsymmetricTextCipher {

    /**
     * 使用指定公钥加密，常用于【使用对方公钥加密】
     *
     * @param text      需加密的数据
     * @param publicKey 对方的公钥(base64编码过的)
     * @return 密文
     * @throws AsymmetricCryptoException 加解密出错
     */
    String encrypt(String text, String publicKey) throws AsymmetricCryptoException;
}
