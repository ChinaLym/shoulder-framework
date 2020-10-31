package org.shoulder.crypto;


import org.shoulder.crypto.exception.CipherRuntimeException;

/**
 * 加解密门面接口
 *
 * @author lym
 */
public interface CryptoFacade {

    // ================================ 本地存储加解密 =====================================

    /**
     * 本地存储加密
     *
     * @param text 待加密数据，不能为null，否则 NPE
     * @return 参数 text 加密后的密文
     * @throws CipherRuntimeException 加密异常
     */
    String encryptLocal(String text) throws CipherRuntimeException;

    /**
     * 本地存储加密解密
     *
     * @param cipherText 密文，不能为null，否则 NPE
     * @return 参数 cipherText 解密后的明文
     * @throws CipherRuntimeException 加密异常
     */
    String decryptLocal(String cipherText) throws CipherRuntimeException;

    /**
     * 确保本地加密功能正常使用
     * 推荐初始化时调用，可优化第一次加解密性能。
     */
    void initLocal();

    // ========================= 非对称加解密，多用于与前端交互，传输对称秘钥、签名、验签 =================

    /**
     * 获取 RSA 公钥
     *
     * @return 公钥
     * @throws CipherRuntimeException e
     */
    String getPk() throws CipherRuntimeException;

    /**
     * 加密
     *
     * @param text 待加密数据
     * @return 加密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    String encryptAsymmetric(String text) throws CipherRuntimeException;


    /**
     * RSA加密（公钥加密）
     *
     * @param text      需加密的数据
     * @param publicKey 对方的公钥
     * @return 密文
     * @throws CipherRuntimeException 加解密出错
     */
    String encryptAsymmetric(String text, String publicKey) throws CipherRuntimeException;


    /**
     * 解密
     *
     * @param cipherText 待解密的数据，密文
     * @return 解密后的
     * @throws CipherRuntimeException RsaCryptoException
     */
    String decryptAsymmetric(String cipherText) throws CipherRuntimeException;

    /**
     * 签名
     *
     * @param text 签名内容
     * @return 签名结果
     * @throws CipherRuntimeException 加解密出错
     */
    String signAsymmetric(String text) throws CipherRuntimeException;

    /**
     * 签名验证
     *
     * @param text      内容
     * @param signature 签名
     * @return 是否合法
     * @throws CipherRuntimeException 加解密出错
     */
    boolean verifyAsymmetric(String text, String signature) throws CipherRuntimeException;


    // ================================ todo 新功能：消息认证，用于接口验证（非加密） =====================================

    // 可选方案：各个服务部署共同的密钥至服务器特定路径下，如 /etc/shoulder_security_root_data.json

}
