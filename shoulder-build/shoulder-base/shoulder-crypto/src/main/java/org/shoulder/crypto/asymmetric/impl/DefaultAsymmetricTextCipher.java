package org.shoulder.crypto.asymmetric.impl;

import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.shoulder.core.constant.ByteSpecification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RSA 的加解密以及签名工具实现：用于与前端交互。填充方式为 jdk 默认（RSA/None/PKCS1Padding）
 * @author lym
 */
public class DefaultAsymmetricTextCipher implements AsymmetricTextCipher {

	private static final Logger log = LoggerFactory.getLogger(DefaultAsymmetricTextCipher.class);

	/** 非对称加密处理器 */
	private final AsymmetricCryptoProcessor processor;

	/** 秘钥对 id ：用于获取加解密的公钥私钥对。分布式时无需关心其他应用是否会有冲突 */
	private final String defaultKeyId;

	public DefaultAsymmetricTextCipher(AsymmetricCryptoProcessor processor) {
		this(processor, "defaultKeyId");
	}

	public DefaultAsymmetricTextCipher(AsymmetricCryptoProcessor processor, String defaultKeyId) {
		this.defaultKeyId = defaultKeyId;
		this.processor = processor;
		try {
			this.processor.buildKeyPair(defaultKeyId);
		} catch (Exception e) {
			log.error("asymmetric crypto init fail");
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getPublicKey() throws KeyPairException {
		return processor.getPublicKeyString(defaultKeyId);
	}

	@Override
	public String decrypt(String cipher) throws AsymmetricCryptoException {
		if(StringUtils.isNotBlank(cipher)) {
			byte[] text = processor.decrypt(defaultKeyId, ByteSpecification.decodeToBytes(cipher));
			return new String(text, CHARSET_UTF_8);
		}
		return cipher;
	}

	@Override
	public String encrypt(String text) throws AsymmetricCryptoException {
		if(StringUtils.isNotBlank(text)) {
			byte[] cipher = processor.encrypt(defaultKeyId, text.getBytes(CHARSET_UTF_8));
			return ByteSpecification.encodeToString(cipher);
		}
		return text;
	}

	@Override
	public String encrypt(String text, String publicKey) throws AsymmetricCryptoException {
		if(StringUtils.isEmpty(publicKey)) {
			throw new NullPointerException("asymmetricEncrypt: publicKey is null!");
		}
		if(StringUtils.isEmpty(text)){
			return text;
		}
		byte[] cipher = processor.encrypt(text.getBytes(CHARSET_UTF_8), ByteSpecification.decodeToBytes(publicKey));
		return ByteSpecification.encodeToString(cipher);
	}

	@Override
	public String sign(String content) throws AsymmetricCryptoException {
		return new String(processor.sign(defaultKeyId, content.getBytes(CHARSET_UTF_8)), CHARSET_UTF_8);
	}

	@Override
	public boolean verify(String content, String signature) throws AsymmetricCryptoException {
		return processor.verify(defaultKeyId, content.getBytes(CHARSET_UTF_8), signature.getBytes(CHARSET_UTF_8));
	}

}
