package org.shoulder.crypto.asymmetric;


import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.TextCipher;

/**
 * 非对称加解密：主要用于与前端交互
 *  RSA 算法 jdk 默认填充方式为（RSA/None/PKCS1Padding），会在加密密文中填充随机数导致即使同一个数据，同一个秘钥，加密结果也不同
 * @author lym
 */
public interface AsymmetricTextCipher extends TextCipher {
	
	/**
	 * 生成公钥
	 * @return 公钥
	 * @throws KeyPairException RsaKeyPairException
	 */
	String getPublicKey() throws KeyPairException;
	
	/**
	 * 解密
	 * @param cipher   	待解密的数据，密文
	 * @return 解密后的
	 * @throws AsymmetricCryptoException RsaCryptoException
	 */
	@Override
	String decrypt(String cipher) throws AsymmetricCryptoException;
	
	/**
	 * 加密
	 * @param text                待加密数据
	 * @return 加密后的
	 * @throws AsymmetricCryptoException RsaCryptoException
	 */
	@Override
	String encrypt(String text) throws AsymmetricCryptoException;

	/**
	 * RSA加密（公钥加密）
	 * @param text 需加密的数据
	 * @param publicKey 对方的公钥(base64过的)
	 * @return 密文
	 * @throws AsymmetricCryptoException 加解密出错
	 */
	String encrypt(String text, String publicKey) throws AsymmetricCryptoException;


	/**
	 * 签名
	 * @param content 签名内容
	 * @return 签名结果
	 * @throws AsymmetricCryptoException 加解密出错
	 */
	String sign(String content) throws AsymmetricCryptoException;

	/**
	 * 签名验证
	 * @param content   内容
	 * @param signature 签名
	 * @return 是否合法
	 * @throws AsymmetricCryptoException 加解密出错
	 */
	boolean verify(String content, String signature) throws AsymmetricCryptoException;
}
