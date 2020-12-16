package org.shoulder.crypto.aes;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Arrays;

/**
 * 对称加解密工具实现
 *
 * @author lym
 */
public class DefaultSymmetricCryptoProcessor implements SymmetricCryptoProcessor, ByteSpecification {

    // BC
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 算法名称，用于生成密钥
     */
    private final String algorithm;

    /**
     * 指定算法实现提供商
     */
    private final String provider;

    /**
     * 密钥位数
     */
    private final int[] keyLengthSupports;

    /**
     * 算法实现
     */
    private final String transformation;

    /**
     * 需要初始化向量，固定长度 16*8=128
     */
    private final boolean needIv;

    public DefaultSymmetricCryptoProcessor(String algorithm, int[] keyLengthSupports, String transformation, String provider) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.keyLengthSupports = keyLengthSupports;
        this.transformation = transformation;
        needIv = StringUtils.contains(transformation, "CBC") || StringUtils.contains(transformation, "CFB");
    }

    public DefaultSymmetricCryptoProcessor(String transformation) {
        this.provider = "BC";
        this.algorithm = transformation.substring(0, transformation.indexOf("/"));
        this.keyLengthSupports = new int[]{16, 24, 32};
        this.transformation = transformation;
        needIv = StringUtils.contains(transformation, "/CBC/") || StringUtils.contains(transformation, "/CFB/");
    }

    // ------------------ 提供两个推荐使用的安全加密方案 ------------------

    public static DefaultSymmetricCryptoProcessor aes_256_CBC_PKCS5Padding() {
        return new DefaultSymmetricCryptoProcessor("SM4/CBC/PKCS5Padding");
    }

    public static DefaultSymmetricCryptoProcessor sm4_256_CBC_PKCS5Padding() {
        return new DefaultSymmetricCryptoProcessor("SM4/CBC/PKCS5Padding");
    }

    /**
     * 对称加密
     *
     * @param content 明文
     * @param key     密钥
     * @param iv      向量
     * @return 密文
     */
    @Override
    public byte[] encrypt(byte[] key, byte[] iv, byte[] content) throws SymmetricCryptoException {
        return doCipher(Cipher.ENCRYPT_MODE, key, iv, content);
    }

    /**
     * 对称解密
     *
     * @param key        密钥
     * @param iv         向量
     * @param cipherText 密文
     * @return 明文
     */
    @Override
    public byte[] decrypt(byte[] key, byte[] iv, byte[] cipherText) throws SymmetricCryptoException {
        return doCipher(Cipher.DECRYPT_MODE, key, iv, cipherText);
    }

    /**
     * 加密或解密
     *
     * @param decryptMode 加密/解密
     * @param key         密钥
     * @param iv          向量
     * @param content     需要处理的内容，密文/明文
     * @return 明文
     */
    public byte[] doCipher(int decryptMode, byte[] key, byte[] iv, byte[] content) throws SymmetricCryptoException {
        try {
            validParam(key, iv);
            SecretKeySpec secretKey = new SecretKeySpec(key, algorithm);
            Cipher cipher = Cipher.getInstance(transformation, provider);
            if (needIv) {
                IvParameterSpec ivParameter = new IvParameterSpec(iv);
                cipher.init(decryptMode, secretKey, ivParameter);
            } else {
                cipher.init(decryptMode, secretKey);
            }
            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new SymmetricCryptoException("symmetricCryptoException doCipher(mode=" + decryptMode + ") Exception!", e);
        }
    }

    /**
     * 参数校验
     *
     * @param key aes 密钥必须 128/192/256 位
     * @param iv  加密向量，必须 128 位 16byte
     */
    private void validParam(byte[] key, byte[] iv) {
        Assert.notNull(key, "the parameter 'key' can't be null!");
        for (int i = 0; i < keyLengthSupports.length; i++) {
            if (key.length == keyLengthSupports[i]) {
                break;
            }
            if (i == keyLengthSupports.length - 1) {
                throw new IllegalArgumentException("the length of parameter 'key' not support, only support " + Arrays.toString(keyLengthSupports));
            }
        }
        if (needIv) {
            Assert.notNull(iv, "the parameter 'iv' can't be null!");
            Assert.isTrue(iv.length == 16, "the parameter 'iv' must be 128 bit(16 byte)");
        }
    }
}
