package org.shoulder.crypto.negotiation.util;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.dto.KeyExchangeRequest;
import org.shoulder.crypto.negotiation.support.dto.KeyExchangeResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

/**
 * 为 String 提供适配
 *
 * @author lym
 */
public class TransportCryptoUtil {

    private final TransportCryptoByteUtil adapter;

    public TransportCryptoUtil(TransportCryptoByteUtil adapter) {
        this.adapter = adapter;
    }

    // =========================== 加解密算法相关 ==================================

    /**
     * 生成数据密钥
     */
    public static byte[] generateDataKey(int size) {
        return TransportCryptoByteUtil.generateDataKey(size);
    }

    /**
     * 生成数据密钥的密文（用于协商完毕，每次请求中）
     *
     * @return xDk
     */
    public static String encryptDk(KeyExchangeResult keyExchangeResult, byte[] dataKey) throws AesCryptoException {
        return ByteSpecification.encodeToString(TransportCryptoByteUtil.encryptDk(keyExchangeResult, dataKey));
    }

    /**
     * 解密 xDk（用于协商完毕，每次请求中）
     *
     * @return dataKey
     */
    public static byte[] decryptDk(KeyExchangeResult keyExchangeResult, String xDk) throws SymmetricCryptoException {
        return TransportCryptoByteUtil.decryptDk(keyExchangeResult, ByteSpecification.decodeToBytes(xDk));
    }

    /**
     * 加密数据
     */
    public static String encrypt(KeyExchangeResult keyExchangeResult, byte[] dataKey, String toCipher) throws AesCryptoException {
        return ByteSpecification.encodeToString(TransportCryptoByteUtil.encrypt(keyExchangeResult, dataKey, toCipher.getBytes(ByteSpecification.STD_CHAR_SET)));
    }

    /**
     * 解密数据
     */
    public static String decrypt(KeyExchangeResult keyExchangeResult, byte[] dataKey, String cipherText) throws AesCryptoException {
        return new String(TransportCryptoByteUtil.decrypt(keyExchangeResult, dataKey, ByteSpecification.decodeToBytes(cipherText)), ByteSpecification.STD_CHAR_SET);
    }


    // =========================== 握手相关 ==================================

    /**
     * 创建一个协商请求（客户端调用）
     *
     * @return 协商请求体、请求头中需要的内容
     */
    public KeyExchangeRequest createRequest() throws AsymmetricCryptoException {
        return adapter.createRequest();
    }


    /**
     * 验证 token（服务端处理协商请求前）
     *
     * @param request 待校验的请求：{@link #createRequest} 方法生成结果
     */
    public boolean verifyToken(KeyExchangeRequest request) throws AsymmetricCryptoException {
        return adapter.verifyRequestToken(request);
    }

    /**
     * 根据协商请求准备协商参数：确定加密算法、密钥长度、协商有效期（服务端调用）
     *
     * @param keyExchangeRequest 待协商的请求：{@link #createRequest} 方法生成结果
     * @return 协商参数 {@link #negotiation} 方法的入参
     */
    public KeyExchangeResponse prepareNegotiation(KeyExchangeRequest keyExchangeRequest) throws AsymmetricCryptoException {
        return adapter.prepareNegotiation(keyExchangeRequest);
    }

    /**
     * 协商密钥与 iv（客户端、服务端都会调）
     * 生成 shareKey、根据 shareKey 生成 localKey，localIv
     *
     * @param keyExchangeResponse 协商参数 {@link #prepareNegotiation} 方法的返回值
     * @return 密钥协商结果
     */
    public KeyExchangeResult negotiation(KeyExchangeResponse keyExchangeResponse) throws KeyPairException, NegotiationException {
        return adapter.negotiation(keyExchangeResponse);
    }

    /**
     * 根据缓存内容生成握手响应
     *
     * @param keyExchangeResult 密钥交换的缓存结果
     * @return 服务端返回给客户端的响应，同 {@link #prepareNegotiation}
     */
    public KeyExchangeResponse createResponse(KeyExchangeResult keyExchangeResult) throws AsymmetricCryptoException {
        KeyExchangeResponse response = new KeyExchangeResponse();

        byte[] publicKey = keyExchangeResult.getPublicKey();

        response.setPublicKey(ByteSpecification.encodeToString(publicKey));
        response.setExpireTime((int) (keyExchangeResult.getExpireTime() - System.currentTimeMillis()));
        response.setKeyBytesLength(keyExchangeResult.getKeyLength());
        // todo 【使用范围】不应该写死256，而是支持的密钥算法，如 aes、sm4
        response.setAes("256");

        response.setxSessionId(keyExchangeResult.getxSessionId());
        // todo 【流程】处理 token 生成失败
        String token = generateResponseToken(response);
        response.setToken(token);
        return response;
    }

    /**
     * 生成 token（服务端返回协商响应时生成）todo【封装】考虑放到 {@link #prepareNegotiation} 内实现？
     *
     * @param response {@link #prepareNegotiation} 方法返回值
     * @return token，用于保证 response 不被篡改
     */
    public String generateResponseToken(KeyExchangeResponse response) throws AsymmetricCryptoException {
        return ByteSpecification.encodeToString(adapter.generateResponseToken(response));
    }


    /**
     * 验证 token（客户端接收服务端返回的协商参数响应前）
     *
     * @param response 客户端收到的，由服务端调用 {@link #prepareNegotiation} 方法的返回值
     * @return 是否合法
     */
    public boolean verifyToken(KeyExchangeResponse response) throws AsymmetricCryptoException {
        return adapter.verifyResponseToken(response);
    }


    // ----------------------------------------- 协商完毕 -------------------------------------------------------------

    /**
     * 生成 token（协商完毕，每次发送安全会话请求时）
     *
     * @param xDk 每次请求发过来的临时密钥
     */
    public String generateToken(String xSessionId, @Nullable String xDk) throws AsymmetricCryptoException {
        return ByteSpecification.encodeToString(
            adapter.generateToken(xSessionId, StringUtils.isEmpty(xDk) ? null : ByteSpecification.decodeToBytes(xDk))
        );
    }

    /**
     * 验证 token（协商完毕，客户端验证服务端响应结果）
     */
    public boolean verifyToken(String xSessionId, String xDk, String token, byte[] otherPublicKey) throws AsymmetricCryptoException {
        return adapter.verifyToken(xSessionId, ByteSpecification.decodeToBytes(xDk), ByteSpecification.decodeToBytes(token), otherPublicKey);
    }

    /**
     * 发起请求前，生成头部信息【请求中不带敏感信息】
     *
     * @param keyExchangeResult 密钥交换结果
     * @return 请求头
     * @throws AsymmetricCryptoException 签名出错
     * @throws AesCryptoException 加密 dataKey 出错
     */
    public HttpHeaders generateHeaders(KeyExchangeResult keyExchangeResult) throws AsymmetricCryptoException, AesCryptoException {
        return generateHeaders(keyExchangeResult, null);
    }

    /**
     * 发起请求前，生成头部信息
     *
     * @param keyExchangeResult 密钥交换结果
     * @param dataKey           数据密钥明文，如果为 null 表示请求中不带敏感信息，发起请求或收到请求时无需加密或解密
     * @return 请求头
     * @throws AsymmetricCryptoException 签名出错
     * @throws AesCryptoException 加密 dataKey 出错
     */
    public HttpHeaders generateHeaders(KeyExchangeResult keyExchangeResult, @Nullable byte[] dataKey) throws AsymmetricCryptoException, AesCryptoException {
        String xDk = encryptDk(keyExchangeResult, dataKey);
        HttpHeaders headers = new HttpHeaders();
        headers.add(KeyExchangeConstants.TOKEN, generateToken(keyExchangeResult.getxSessionId(), xDk));
        headers.add(KeyExchangeConstants.SECURITY_SESSION_ID, keyExchangeResult.getxSessionId());
        if (ByteUtils.isNotEmpty(dataKey)) {
            headers.add(KeyExchangeConstants.SECURITY_DATA_KEY, xDk);
        }
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return headers;
    }

}
