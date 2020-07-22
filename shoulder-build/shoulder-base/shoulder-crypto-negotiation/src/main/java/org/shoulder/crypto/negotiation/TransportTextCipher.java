package org.shoulder.crypto.negotiation;


import org.shoulder.crypto.TextCipher;
import org.shoulder.crypto.negotiation.exception.TransportCryptoException;
import lombok.NonNull;

/**
 * 传输加解密：仅用于传输
 * 子类职责：负责实现传输加解密
 *
 * @author lym
 */
public interface TransportTextCipher extends TextCipher {
	
	/**
	 * 加密
	 * @param text	待加密数据，不能为null，否则 NPE
	 * @return		参数 text 加密后的密文
	 * @throws TransportCryptoException aes异常
	 */
	@Override
	String encrypt(@NonNull String text) throws TransportCryptoException;
	
	/**
	 * 解密
	 * @param cipherText	密文，不能为null，否则 NPE
	 * @return				参数 cipherText 解密后的明文
	 * @throws TransportCryptoException aes异常
	 */
	@Override
	String decrypt(@NonNull String cipherText) throws TransportCryptoException;

}
