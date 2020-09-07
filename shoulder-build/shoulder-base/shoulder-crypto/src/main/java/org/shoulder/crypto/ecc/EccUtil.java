package org.shoulder.crypto.ecc;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.core.constant.ByteSpecification;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author lym
 */
public class EccUtil implements ByteSpecification {

	private static final String ALGORITHM = "EC";
	private static final int KEY_LENGTH = 256;
	private static final String PROVIDER = "BC";
	private static final String TRANSFORMATION = "ECIES";


	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	//生成秘钥对
	public static KeyPair getKeyPair() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		keyPairGenerator.initialize(KEY_LENGTH, new SecureRandom());
		return keyPairGenerator.generateKeyPair();
	}

    //获取公钥(Base64编码)
	public static String getPublicKey(KeyPair keyPair){
		PublicKey publicKey = keyPair.getPublic();
		byte[] bytes = publicKey.getEncoded();
		return ByteSpecification.encodeToString(bytes);
	}

    //获取私钥(Base64编码)
	public static String getPrivateKey(KeyPair keyPair){
		PrivateKey privateKey = keyPair.getPrivate();
		byte[] bytes = privateKey.getEncoded();
		return ByteSpecification.encodeToString(bytes);
	}

	//将Base64编码后的公钥转换成PublicKey对象
	public static PublicKey string2PublicKey(String pubStr) throws Exception{
		byte[] keyBytes = ByteSpecification.decodeToBytes(pubStr);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER);
		return keyFactory.generatePublic(keySpec);
	}

    //将Base64编码后的私钥转换成PrivateKey对象
	public static PrivateKey string2PrivateKey(String priStr) throws Exception{
		byte[] keyBytes = ByteSpecification.decodeToBytes(priStr);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER);
		return keyFactory.generatePrivate(keySpec);
	}

    //公钥加密
	public static byte[] publicEncrypt(byte[] content, PublicKey publicKey) throws Exception{
		Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(content);
	}

    //私钥解密
	public static byte[] privateDecrypt(byte[] content, PrivateKey privateKey) throws Exception{
		Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(content);
	}

	public static void test() throws Exception {
		KeyPair keyPair = EccUtil.getKeyPair();
		String publicKeyStr = EccUtil.getPublicKey(keyPair);
		String privateKeyStr = EccUtil.getPrivateKey(keyPair);
		//System.out.println("ECC公钥Base64编码:" + publicKeyStr);
		//System.out.println("ECC私钥Base64编码:" + privateKeyStr);

		PublicKey publicKey = string2PublicKey(publicKeyStr);
		PrivateKey privateKey = string2PrivateKey(privateKeyStr);

		byte[] publicEncrypt = publicEncrypt("hello world".getBytes(), publicKey);
		byte[] privateDecrypt = privateDecrypt(publicEncrypt, privateKey);
		//System.out.println(new String(privateDecrypt));
	}

	public static void main(String[] args) throws Exception {
		// 预热
		for (int i = 0; i < 1000; i++) {
			test();
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			test();
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}
}
