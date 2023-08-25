package org.shoulder.crypto.symmetric.impl;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.symmetric.SymmetricCipher;
import org.shoulder.crypto.symmetric.exception.SymmetricCryptoException;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对称加解密工具实现
 *
 * @author lym
 */
public class DefaultSymmetricCipher implements SymmetricCipher, ByteSpecification {

    /**
     * 享元工厂模式
     */
    private static final ConcurrentHashMap<String, DefaultSymmetricCipher> FLYWEIGHT_CACHE = new ConcurrentHashMap<>();

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
     * 需要初始化向量，固定长度 16*8=128，todo 【优化-校验】OCB 必须小于 128，解密必须传 iv，可以为空 byte[]
     */
    private final boolean needIv;

    public DefaultSymmetricCipher(String algorithm, int[] keyLengthSupports, String transformation,
                                  String provider, boolean needIv) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.keyLengthSupports = keyLengthSupports;
        this.transformation = transformation;
        this.needIv = needIv;
    }

    // ------------------ 提供两个推荐使用的安全加密方案 ------------------

    public DefaultSymmetricCipher(String transformation) {
        this.transformation = transformation;
        this.provider = "BC";
        this.algorithm = transformation.substring(0, transformation.indexOf("/"));
        this.keyLengthSupports = new int[]{16, 24, 32};
        needIv = StringUtils.contains(transformation, "/CBC/")
            || StringUtils.contains(transformation, "/CFB/")
            || StringUtils.contains(transformation, "/GCM/")
        ;
    }

    @Nonnull
    public static DefaultSymmetricCipher getFlyweight(String encryptionScheme) {
        return FLYWEIGHT_CACHE.computeIfAbsent(encryptionScheme, DefaultSymmetricCipher::new);
    }

    /**
     * 对称加密
     *
     * @param plainText 明文
     * @param key       密钥
     * @param iv        向量
     * @return 密文
     */
    @Override
    public byte[] encrypt(byte[] key, byte[] iv, byte[] plainText) throws SymmetricCryptoException {
        return doCipher(Cipher.ENCRYPT_MODE, key, iv, plainText);
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
     * 参数校验 （只检查key长度）
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
            // 注意 OCB 特殊，故不能一概而论（但由于其带有专利，故不考虑）
            Assert.isTrue(iv.length == 16, "the parameter 'iv' must be 128 bit(16 byte)");
        }
    }
}
