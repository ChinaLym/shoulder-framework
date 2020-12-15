package org.shoulder.crypto.negotiation.util;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.dto.NegotiationRequest;
import org.shoulder.crypto.negotiation.support.dto.NegotiationResponse;
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

    // =========================== 协商阶段相关 ===========================

    /**
     * 创建一个协商请求（客户端调用）
     *
     * @return 协商请求体、请求头中需要的内容
     */
    public NegotiationRequest createRequest() throws AsymmetricCryptoException {
        return adapter.createRequest();
    }


    /**
     * 验证 token（服务端处理协商请求前）
     *
     * @param request 待校验的请求：{@link #createRequest} 方法生成结果
     */
    public boolean verifyToken(NegotiationRequest request) throws AsymmetricCryptoException {
        return adapter.verifyRequestToken(request);
    }

    /**
     * 根据协商请求准备协商参数：确定加密算法、密钥长度、协商有效期（服务端调用）
     *
     * @param negotiationRequest 待协商的请求：{@link #createRequest} 方法生成结果
     * @return 协商参数 {@link #negotiation} 方法的入参
     */
    public NegotiationResponse prepareNegotiation(NegotiationRequest negotiationRequest) throws AsymmetricCryptoException {
        return adapter.prepareNegotiation(negotiationRequest);
    }

    /**
     * 协商密钥与 iv（客户端、服务端都会调）
     * 生成 shareKey、根据 shareKey 生成 localKey，localIv
     *
     * @param negotiationResponse 协商参数 {@link #prepareNegotiation} 方法的返回值
     * @return 密钥协商结果
     */
    public NegotiationResult negotiation(NegotiationResponse negotiationResponse) throws KeyPairException, NegotiationException {
        return adapter.negotiation(negotiationResponse);
    }

    /**
     * 根据缓存内容生成握手响应
     *
     * @param negotiationResult 密钥交换的缓存结果
     * @return 服务端返回给客户端的响应，同 {@link #prepareNegotiation}
     */
    public NegotiationResponse createResponse(NegotiationResult negotiationResult) throws AsymmetricCryptoException {
        NegotiationResponse response = new NegotiationResponse();

        byte[] publicKey = negotiationResult.getPublicKey();

        response.setPublicKey(ByteSpecification.encodeToString(publicKey));
        response.setExpireTime((int) (negotiationResult.getExpireTime() - System.currentTimeMillis()));
        response.setKeyBytesLength(negotiationResult.getKeyLength());
        // todo 【使用范围】不应该写死256，而是支持的密钥算法，如 aes、sm4
        response.setEncryptionScheme("256");

        response.setxSessionId(negotiationResult.getxSessionId());
        // todo 【流程】处理 token 生成失败
        String token = generateResponseToken(response);
        response.setToken(token);
        return response;
    }

    /**
     * 生成 token（服务端返回协商响应时生成）todo【可读性】考虑放到 {@link #prepareNegotiation} 内实现？
     *
     * @param response {@link #prepareNegotiation} 方法返回值
     * @return token，用于保证 response 不被篡改
     */
    public String generateResponseToken(NegotiationResponse response) throws AsymmetricCryptoException {
        return ByteSpecification.encodeToString(adapter.generateResponseToken(response));
    }


    /**
     * 验证 token（客户端接收服务端返回的协商参数响应前）
     *
     * @param response 客户端收到的，由服务端调用 {@link #prepareNegotiation} 方法的返回值
     * @return 是否合法
     */
    public boolean verifyToken(NegotiationResponse response) throws AsymmetricCryptoException {
        return adapter.verifyResponseToken(response);
    }


    // =========================== 协商完毕，遵守 DH 协议 ===========================

    /**
     * 生成数据密钥（用于协商完毕，产生加密报文时使用）
     *
     * @return 数据密钥（真正加密数据的）
     */
    public static byte[] generateDataKey(int size) {
        return TransportCryptoByteUtil.generateDataKey(size);
    }

    /**
     * 生成数据密钥的密文（用于协商完毕，产生加密报文时使用，每次请求中）
     *
     * @param negotiationResult 密钥协商结果
     * @param dataKey           数据密钥
     * @return 数据密钥密文 xDk
     */
    public static String encryptDk(NegotiationResult negotiationResult, byte[] dataKey) throws AesCryptoException {
        return ByteSpecification.encodeToString(TransportCryptoByteUtil.encryptDk(negotiationResult, dataKey));
    }

    /**
     * 解密 xDk（处理加密报文时使用）
     *
     * @param negotiationResult 密钥协商结果
     * @param xDk               数据密钥密文
     * @return 数据密钥明文 dataKey
     */
    public static byte[] decryptDk(NegotiationResult negotiationResult, String xDk) throws SymmetricCryptoException {
        return TransportCryptoByteUtil.decryptDk(negotiationResult, ByteSpecification.decodeToBytes(xDk));
    }

    /**
     * 加密数据
     *
     * @param negotiationResult 密钥协商结果
     * @param dataKey           数据密钥明文
     * @param text              数据明文
     * @return 数据密文 cipherText
     */
    public static String encrypt(NegotiationResult negotiationResult, byte[] dataKey, String text) throws AesCryptoException {
        return ByteSpecification.encodeToString(TransportCryptoByteUtil.encrypt(negotiationResult, dataKey, text.getBytes(ByteSpecification.STD_CHAR_SET)));
    }

    /**
     * 解密数据
     *
     * @param negotiationResult 密钥协商结果
     * @param dataKey           数据密钥明文
     * @param cipherText        数据密文
     * @return 数据明文 text
     */
    public static String decrypt(NegotiationResult negotiationResult, byte[] dataKey, String cipherText) throws AesCryptoException {
        return new String(TransportCryptoByteUtil.decrypt(negotiationResult, dataKey, ByteSpecification.decodeToBytes(cipherText)), ByteSpecification.STD_CHAR_SET);
    }


    // ------------------ 安全保障-防监听篡改/防抵赖 ----------------

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
     * @param negotiationResult 密钥交换结果
     * @return 请求头
     * @throws AsymmetricCryptoException 签名出错
     * @throws AesCryptoException        加密 dataKey 出错
     */
    public HttpHeaders generateHeaders(NegotiationResult negotiationResult) throws AsymmetricCryptoException, AesCryptoException {
        return generateHeaders(negotiationResult, null);
    }

    /**
     * 发起请求前，生成头部信息
     *
     * @param negotiationResult 密钥交换结果
     * @param dataKey           数据密钥明文，如果为 null 表示请求中不带敏感信息，发起请求或收到请求时无需加密或解密
     * @return 请求头
     * @throws AsymmetricCryptoException 签名出错
     * @throws AesCryptoException        加密 dataKey 出错
     */
    public HttpHeaders generateHeaders(NegotiationResult negotiationResult, @Nullable byte[] dataKey) throws AsymmetricCryptoException, AesCryptoException {
        String xDk = encryptDk(negotiationResult, dataKey);
        HttpHeaders headers = new HttpHeaders();
        headers.add(NegotiationConstants.TOKEN, generateToken(negotiationResult.getxSessionId(), xDk));
        headers.add(NegotiationConstants.SECURITY_SESSION_ID, negotiationResult.getxSessionId());
        if (ByteUtils.isNotEmpty(dataKey)) {
            headers.add(NegotiationConstants.SECURITY_DATA_KEY, xDk);
        }
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return headers;
    }

}
