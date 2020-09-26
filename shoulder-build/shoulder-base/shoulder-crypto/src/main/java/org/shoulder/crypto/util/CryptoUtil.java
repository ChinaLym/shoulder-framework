package org.shoulder.crypto.util;


import org.shoulder.core.util.SpringUtils;
import org.shoulder.crypto.CryptoFacade;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.exception.CipherRuntimeException;

/**
 * 加解密工具门面
 *
 * @author lym
 */
public final class CryptoUtil {

    private static CryptoFacade crypto;

    static {
        crypto = SpringUtils.getBean(CryptoFacade.class);
    }

    // ================================ 存储加解密 =====================================

    /**
     * 本地存储加密
     *
     * @param text 待加密数据，不能为null，否则 NPE
     * @return 参数 text 加密后的密文
     * @throws CipherRuntimeException 加密异常
     */
    public static String encryptAes(String text) throws CipherRuntimeException {
        return crypto.encryptLocal(text);
    }

    /**
     * 本地存储加密解密
     *
     * @param cipherText 密文，不能为null，否则 NPE
     * @return 参数 cipherText 解密后的明文
     * @throws CipherRuntimeException 加密异常
     */
    public static String decryptAes(String cipherText) throws CipherRuntimeException {
        return crypto.decryptLocal(cipherText);
    }

    /**
     * 确保本地加密功能正常使用
     * 推荐初始化时调用，可优化第一次加解密性能。
     */
    public static void initAes() {
        crypto.initLocal();
    }

    // ================================ 传输加解密（如前后交互） =====================================

    /**
     * 获取 RSA 公钥
     *
     * @return 公钥
     */
    public static String getPk() throws KeyPairException {
        return crypto.getPk();
    }

    /**
     * 加密
     *
     * @param text 待加密数据
     * @return 加密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    public static String encryptRsa(String text) throws CipherRuntimeException {
        return crypto.encryptAsymmetric(text);
    }

    /**
     * 解密
     *
     * @param cipherText 待解密的数据，密文
     * @return 解密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    public static String decryptRsa(String cipherText) throws CipherRuntimeException {
        return crypto.decryptAsymmetric(cipherText);
    }


    /**
     * RSA加密（公钥加密）
     *
     * @param text      需加密的数据
     * @param publicKey 对方的公钥
     * @return 密文
     * @throws CipherRuntimeException 加解密出错
     */
    public String encryptRsa(String text, String publicKey) throws CipherRuntimeException {
        return crypto.encryptAsymmetric(text, publicKey);
    }

    /**
     * 签名
     *
     * @param text 签名内容
     * @return 签名结果
     * @throws CipherRuntimeException 加解密出错
     */
    public String signRsa(String text) throws CipherRuntimeException {
        return crypto.signAsymmetric(text);
    }

    /**
     * 签名验证
     *
     * @param text      内容
     * @param signature 签名
     * @return 是否合法
     * @throws CipherRuntimeException 加解密出错
     */
    public boolean verifyRsa(String text, String signature) throws CipherRuntimeException {
        return crypto.verifyAsymmetric(text, signature);
    }

}
