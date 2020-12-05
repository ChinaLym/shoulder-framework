package org.shoulder.crypto.util;


import org.shoulder.core.util.ContextUtils;
import org.shoulder.crypto.asymmetric.AsymmetricTextCipher;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.exception.CipherRuntimeException;
import org.shoulder.crypto.local.LocalTextCipher;

/**
 * 加解密工具
 *
 * @author lym
 */
public final class CryptoUtil {

    // ================================ 本地存储加解密 =====================================

    /**
     * 本地存储加密
     *
     * @param text 待加密数据，不能为null，否则 NPE
     * @return 参数 text 加密后的密文
     * @throws CipherRuntimeException 加密异常
     */
    public static String localEncrypt(String text) throws CipherRuntimeException {
        return getLocal().encrypt(text);
    }

    /**
     * 本地存储加密解密
     *
     * @param cipherText 密文，不能为null，否则 NPE
     * @return 参数 cipherText 解密后的明文
     * @throws CipherRuntimeException 加密异常
     */
    public static String localDecrypt(String cipherText) throws CipherRuntimeException {
        return getLocal().decrypt(cipherText);
    }

    // ================================ 传输加解密（如前后交互） =====================================

    /**
     * 获取 RSA 公钥
     *
     * @return 公钥
     */
    public static String getPublicKey() throws KeyPairException {
        return getAsymmetric().getPublicKey();
    }

    /**
     * 加密
     *
     * @param text 待加密数据
     * @return 加密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    public static String asymmetricEncrypt(String text) throws CipherRuntimeException {
        return getAsymmetric().encrypt(text);
    }

    /**
     * 解密
     *
     * @param cipherText 待解密的数据，密文
     * @return 解密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    public static String asymmetricDecrypt(String cipherText) throws CipherRuntimeException {
        return getAsymmetric().decrypt(cipherText);
    }


    /**
     * RSA加密（公钥加密）
     *
     * @param text      需加密的数据
     * @param publicKey 对方的公钥
     * @return 密文
     * @throws CipherRuntimeException 加解密出错
     */
    public String asymmetricEncrypt(String text, String publicKey) throws CipherRuntimeException {
        return getAsymmetric().encrypt(text, publicKey);
    }

    /**
     * 签名
     *
     * @param text 签名内容
     * @return 签名结果
     * @throws CipherRuntimeException 加解密出错
     */
    public String sign(String text) throws CipherRuntimeException {
        return getAsymmetric().sign(text);
    }

    /**
     * 签名验证
     *
     * @param text      内容
     * @param signature 签名
     * @return 是否合法
     * @throws CipherRuntimeException 加解密出错
     */
    public boolean verify(String text, String signature) throws CipherRuntimeException {
        return getAsymmetric().verify(text, signature);
    }

    // ------------------------- singleTon holder --------------------------

    private static LocalTextCipher getLocal() {
        return LocalTextCipherHolder.INSTANCE;
    }

    private static AsymmetricTextCipher getAsymmetric() {
        return AsymmetricCipherHolder.INSTANCE;
    }

    private static class LocalTextCipherHolder {
        private static final LocalTextCipher INSTANCE = ContextUtils.getBean(LocalTextCipher.class);
    }

    private static class AsymmetricCipherHolder {
        private static final AsymmetricTextCipher INSTANCE = ContextUtils.getBean(AsymmetricTextCipher.class);
    }

}
