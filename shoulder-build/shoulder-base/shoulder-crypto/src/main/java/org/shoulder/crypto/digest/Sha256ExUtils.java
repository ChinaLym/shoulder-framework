package org.shoulder.crypto.digest;

import org.shoulder.core.util.ByteUtils;
import org.shoulder.core.constant.ByteSpecification;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Secure Hash Algorithm
 * 安全加固的 SHA256
 * 即使对同一个字符串hash，hash的结果也几乎完全不一致
 * 		原理时会添加随机盐，但这会导致如果没有当时的随机盐，无法对同一个数据再次hash校验。
 * @author lym
 */
public class Sha256ExUtils implements ByteSpecification {

	private static final int SALT_BIT = 8;
	/** 256位 相当于 32个 byte */
	private static final int SHA256_RESULT_LENGTH = 256 / 8;
	/** salt 填充间隔 */
	private static final int SALT_STEP = SHA256_RESULT_LENGTH / SALT_BIT;
	/**
	 * 加盐哈西
	 */
	public static byte[] digest(byte[] text) {
		if(text == null || text.length == 0){
			throw new IllegalArgumentException("text can't be empty!");
		}
		return doHashWithSalt(text, randomSalt());
	}

	/**
	 * 加盐哈西
	 */
	public static String digest(String text) {
		return ByteSpecification.encodeToString(digest(text.getBytes(ByteSpecification.STD_CHAR_SET)));
	}


	/**
	 *	验证原文hash后是否与密文相同
	 * @param text		cipher的明文
	 * @param cipher	text hash厚的
	 */
	public static boolean verify(byte[] text, byte[] cipher){
		byte[] salt = getSalt(cipher);
		byte[] hashValue = doHashWithSalt(text, salt);
		return Arrays.equals(hashValue, cipher);
	}

	/**
	 *	验证原文hash后是否与密文相同
	 * @param text		cipher的明文
	 * @param cipher	text hash厚的
	 */
	public static boolean verify(String text, String cipher){
		return verify(text.getBytes(ByteSpecification.STD_CHAR_SET), ByteSpecification.decodeToBytes(cipher));
	}

	public static void main(String[] args) {

		System.out.println("========== 测试多次调用，得到的hash值是否不同 ==========");
		for (int i = 0; i < 30; i++) {
			System.out.println("hashValue —— "+ Sha256ExUtils.digest("123"));
		}

		System.out.println("========== 测试byte进行hash后验证 ==========");
		byte[] textBytes = "123".getBytes(ByteSpecification.STD_CHAR_SET);
		byte[] cipherBytes = Sha256ExUtils.digest(textBytes);
		System.out.println("是否成功：" + Sha256ExUtils.verify(textBytes, cipherBytes));

		System.out.println("========== 测试字符串进行hash后验证 ==========");
		String text = "123";
		String cipher = Sha256ExUtils.digest(text);
		System.out.println("是否成功：" + Sha256ExUtils.verify(text, cipher));

	}


	// ===========================================================================

	/** 生成随机盐 */
	private static byte[] randomSalt() {
		return ByteUtils.randomBytes(SALT_BIT);
	}

	/**
	 * 计算Hash值
	 */
	private static byte[] doHashWithSalt(byte[] toHashBytes, byte[] salt) {
		byte[] withSaltBytes = new byte[toHashBytes.length + salt.length];
		ByteUtils.copy(toHashBytes, 0, withSaltBytes, 0, toHashBytes.length);
		ByteUtils.copy(salt, 0, withSaltBytes, toHashBytes.length, salt.length);
		return decorateWithSalt(doHash(withSaltBytes), salt);
	}

	/**
	 * 在特定位置 添加指定盐
	 */
	private static byte[] decorateWithSalt(byte[] hashBytes, byte[] salt) {
		for (int hashIndex = 0, saltIndex = 0; saltIndex < salt.length && hashIndex < hashBytes.length; saltIndex++, hashIndex += SALT_STEP) {
			hashBytes[hashIndex] = salt[saltIndex];
		}
		return hashBytes;
	}

	/**
	 * 从特定位置 提取盐值
	 */
	public static byte[] getSalt(byte[] hashValue) {
		if(hashValue == null || hashValue.length != SHA256_RESULT_LENGTH){
			throw new IllegalArgumentException("param is not a sha256 result!");
		}
		byte[] salt = new byte[SALT_BIT];
		for (int hashIndex = 0, saltIndex = 0; saltIndex < salt.length && hashIndex < hashValue.length; saltIndex++, hashIndex += SALT_STEP) {
			salt[saltIndex] = hashValue[hashIndex];
		}
		return salt;
	}

	/**
	 * 计算Hash值, SHA-256原生算法
	 */
	private static byte[] doHash(byte[] toHashBytes) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(toHashBytes);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("NoSuchAlgorithmException SHA-256", e);
		}
	}

}
