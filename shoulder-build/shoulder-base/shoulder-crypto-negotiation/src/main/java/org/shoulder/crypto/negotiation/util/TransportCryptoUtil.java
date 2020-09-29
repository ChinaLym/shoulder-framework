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

    /**
     * 创建一个请求
     */
    public KeyExchangeRequest createRequest() throws AsymmetricCryptoException {
        return adapter.createRequest();
    }

    /**
     * 创建一个响应
     */
    public KeyExchangeResponse createResponse(KeyExchangeRequest keyExchangeRequest) throws Exception {
        return adapter.createResponse(keyExchangeRequest);
    }

    /**
     * 协商密钥与 iv
     */
    public KeyExchangeResult negotiation(KeyExchangeResponse keyExchangeResponse) throws KeyPairException, NegotiationException {
        return adapter.negotiation(keyExchangeResponse);
    }

    public KeyExchangeResponse negotiation(KeyExchangeRequest keyExchangeRequest) throws NegotiationException, AsymmetricCryptoException {
        return adapter.negotiation(keyExchangeRequest);
    }


    // =========================== TOKEN ==================================

    /**
     * 生成 token（发起协商请求时）
     */
    public String generateRequestToken(KeyExchangeRequest request) throws AsymmetricCryptoException {
        return ByteSpecification.encodeToString(adapter.generateRequestToken(request));
    }

    /**
     * 验证 token（处理协商请求前）
     */
    public boolean verifyRequestToken(KeyExchangeRequest request) throws AsymmetricCryptoException {
        return adapter.verifyRequestToken(request);
    }

    // -------------------------------

    /**
     * 生成 token（发起协商响应时生成）
     */
    public String generateResponseToken(KeyExchangeResponse response) throws AsymmetricCryptoException {
        return ByteSpecification.encodeToString(adapter.generateResponseToken(response));
    }

    /**
     * 验证 token（确认协商响应请求时）
     */
    public boolean verifyResponseToken(KeyExchangeResponse response) throws AsymmetricCryptoException {
        return adapter.verifyResponseToken(response);
    }

    // -------------------------------

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
     * 验证 token（协商完毕，每次处理请求时）
     */
    public boolean verifyToken(String xSessionId, String xDk, String token) throws AsymmetricCryptoException {
        return adapter.verifyToken(xSessionId, ByteSpecification.decodeToBytes(xDk), ByteSpecification.decodeToBytes(token));
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
