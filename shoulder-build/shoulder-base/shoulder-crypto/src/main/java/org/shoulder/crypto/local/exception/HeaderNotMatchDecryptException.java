package org.shoulder.crypto.local.exception;

import org.shoulder.crypto.aes.exception.SymmetricCryptoException;

/**
 * 本地加解密出错, 密文的头部标记位异常，可能是由于加密方案不匹配（如加密方案更新，用新的加密算法解密旧数据）导致的
 * @author lym
 */
public class HeaderNotMatchDecryptException extends SymmetricCryptoException {

	public HeaderNotMatchDecryptException(String message, Throwable cause) {
		super(message, cause);
	}
	public HeaderNotMatchDecryptException(String message) {
		super(message);
	}
	
}
