package org.shoulder.crypto.aes;

import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Arrays;


/**
 * AES加密工具
 * AES/CBC/PKCS5Padding    128/192/256
 *
 * @author lym
 */
public class AesUtil {

    private static final String AES = "AES";
    private static final int[] KEY_LENGTH = {16, 24, 32};
    private static final int IV_LENGTH = 16;
    private static String ENCRYPT_TYPE = "AES/CBC/PKCS5Padding";

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * AES256 加密
     *
     * @param content 明文
     * @param key     密钥
     * @param iv      向量
     * @return 密文
     */
    public static byte[] encrypt(byte[] content, byte[] key, byte[] iv) throws AesCryptoException {
        try {
            validParam(key, iv);
            SecretKeySpec secretKey = new SecretKeySpec(key, AES);
            IvParameterSpec ivParameter = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameter);
            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new AesCryptoException("Aes encrypt Exception!", e);
        }
    }

    /**
     * AES256 解密
     *
     * @param encryptContent 密文
     * @param key            密钥
     * @param iv             向量
     * @return 明文
     */
    public static byte[] decrypt(byte[] encryptContent, byte[] key, byte[] iv) throws AesCryptoException {
        try {
            validParam(key, iv);
            SecretKeySpec secretKey = new SecretKeySpec(key, AES);
            IvParameterSpec ivParameter = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameter);
            return cipher.doFinal(encryptContent);
        } catch (Exception e) {
            throw new AesCryptoException("Aes decrypt Exception!", e);
        }
    }

    /**
     * 参数校验
     *
     * @param key aes 密钥必须 128/192/256 位
     * @param iv  加密向量，必须 128 位
     */
    private static void validParam(byte[] key, byte[] iv) {
        Assert.notNull(key, "the parameter 'key' can't be null!");
        Assert.notNull(iv, "the parameter 'iv' can't be null!");
        Assert.isTrue(key.length == KEY_LENGTH[0] || key.length == KEY_LENGTH[1] || key.length == KEY_LENGTH[2],
                "the length of parameter 'key' must be " + KEY_LENGTH[0] + " or " + KEY_LENGTH[1] + " or " + KEY_LENGTH[2] + ", your keyLength: " + key.length);

        Assert.isTrue(iv.length == IV_LENGTH, "the length of parameter 'iv' must be " + IV_LENGTH);
    }

    public static void main(String[] args) throws AesCryptoException {
        testCrypto();
    }

    private static void testCrypto() throws AesCryptoException {
        byte[] text = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        byte[] iv = {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6};
        byte[] key = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                1, 2, 3, 4, 5, 6,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                1, 2, 3, 4, 5, 6};
        boolean result = Arrays.equals(decrypt(encrypt(text, key, iv), key, iv), text);
        System.out.println(result);
    }
}
