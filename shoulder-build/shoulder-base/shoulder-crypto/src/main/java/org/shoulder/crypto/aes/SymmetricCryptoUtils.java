package org.shoulder.crypto.aes;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;


/**
 * AES加密工具
 * 默认使用 AES/CBC/PKCS5Padding   支持位数 128/192/256
 *
 * @author lym
 * @deprecated use {@link SymmetricCipher}
 */
public class SymmetricCryptoUtils {

    /**
     * 算法名称
     */
    private static final String AES = "AES";

    /**
     * 仅支持 aes128/192/256
     */
    private static final int[] KEY_LENGTH_SUPPORT = {16, 24, 32};

    /**
     * 向量长度， 16*8=128
     */
    private static final int IV_LENGTH = 16;

    /**
     * 默认算法
     */
    private static String DEFAULT_AES_TYPE = "AES/CBC/PKCS5Padding";

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
    public static byte[] encrypt(byte[] content, byte[] key, byte[] iv) throws SymmetricCryptoException {
        return doCipher(DEFAULT_AES_TYPE, key, iv, Cipher.ENCRYPT_MODE, content);
    }

    /**
     * AES256 解密
     *
     * @param cipherText 密文
     * @param key        密钥
     * @param iv         向量
     * @return 明文
     */
    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] iv) throws SymmetricCryptoException {
        return doCipher(DEFAULT_AES_TYPE, key, iv, Cipher.DECRYPT_MODE, cipherText);
    }


    /**
     * AES 加密
     *
     * @param content 明文
     * @param key     密钥
     * @param iv      向量
     * @return 密文
     */
    public static byte[] encrypt(String aesType, byte[] content, byte[] key, byte[] iv) throws SymmetricCryptoException {
        return doCipher(aesType, key, iv, Cipher.ENCRYPT_MODE, content);
    }

    /**
     * AES 解密
     *
     * @param cipherText 密文
     * @param key        密钥
     * @param iv         向量
     * @return 明文
     */
    public static byte[] decrypt(String aesType, byte[] cipherText, byte[] key, byte[] iv) throws SymmetricCryptoException {
        return doCipher(aesType, key, iv, Cipher.DECRYPT_MODE, cipherText);
    }

    /**
     * AES256 加密或解密
     *
     * @param aesType     aes 算法类型，如 AES/CBC/PKCS5Padding
     * @param key         密钥
     * @param iv          向量
     * @param decryptMode 加密/解密
     * @param content     需要处理的内容，密文/明文
     * @return 明文
     */
    public static byte[] doCipher(String aesType, byte[] key, byte[] iv, int decryptMode, byte[] content) throws SymmetricCryptoException {
        try {
            validParam(key, iv);
            SecretKeySpec secretKey = new SecretKeySpec(key, AES);
            IvParameterSpec ivParameter = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(aesType);
            cipher.init(decryptMode, secretKey, ivParameter);
            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new SymmetricCryptoException("Aes decrypt Exception!", e);
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
        Assert.isTrue(key.length == KEY_LENGTH_SUPPORT[0]
                || key.length == KEY_LENGTH_SUPPORT[1] || key.length == KEY_LENGTH_SUPPORT[2],
            "the length of parameter 'key' must be " + KEY_LENGTH_SUPPORT[0] +
                " or " + KEY_LENGTH_SUPPORT[1] + " or " + KEY_LENGTH_SUPPORT[2] + ", your keyLength: " + key.length);

        Assert.isTrue(iv.length == IV_LENGTH, "the length of parameter 'iv' must be " + IV_LENGTH);
    }

}
