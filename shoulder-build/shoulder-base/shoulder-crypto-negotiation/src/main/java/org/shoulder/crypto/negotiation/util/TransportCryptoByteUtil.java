package org.shoulder.crypto.negotiation.util;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.crypto.aes.SymmetricCryptoUtils;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.dto.NegotiationResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.dto.NegotiationRequest;
import org.shoulder.crypto.negotiation.support.dto.NegotiationResponse;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 传输加解密相关实现。仅为 byte 提供服务
 *
 * @author lym
 */
public class TransportCryptoByteUtil {

    /**
     * 128/192/256，长度为 4 特用于优化随机数性能
     */
    private static final int[] SUPPORT_KEY_BYTE_LENGTH = {16, 16, 24, 32};

    /**
     * 非对称加密器，生成/保存自己的公私钥
     */
    private final AsymmetricCryptoProcessor asymmetricCryptoProcessor;

    /**
     * 服务端密钥协商缓存过期默认时间
     */
    private final Duration negotiationDuration = Duration.ofHours(1);

    /**
     * 支持的对称加密算法
     */
    private final Set<String> encryptionSchemeSupports = Set.of("AES-1", "", "");


    /**
     * 构造器
     *
     * @param asymmetricCryptoProcessor 非对称加密器，生成/保存自己的公私钥
     */
    public TransportCryptoByteUtil(AsymmetricCryptoProcessor asymmetricCryptoProcessor) {
        this.asymmetricCryptoProcessor = asymmetricCryptoProcessor;
    }

    /**
     * 生成数据密钥的密文（用于协商完毕，每次请求中）
     *
     * @return xDk
     */
    public static byte[] generateDataKey(int size) {
        return ByteUtils.randomBytes(size);
    }

    /**
     * 生成数据密钥的密文（DK）
     */
    public static byte[] encryptDk(NegotiationResult negotiationResult, byte[] dataKey) throws SymmetricCryptoException {
        return SymmetricCryptoUtils.encrypt(dataKey, negotiationResult.getShareKey(), negotiationResult.getLocalIv());
    }

    /**
     * 解密 xDk（用于协商完毕，每次请求中）
     *
     * @return dataKey
     */
    public static byte[] decryptDk(NegotiationResult negotiationResult, byte[] xDk) throws SymmetricCryptoException {
        return SymmetricCryptoUtils.decrypt(xDk, negotiationResult.getShareKey(), negotiationResult.getLocalIv());
    }

    /**
     * 加密数据
     */
    public static byte[] encrypt(NegotiationResult negotiationResult, byte[] dataKey, byte[] toCipher) throws SymmetricCryptoException {
        return SymmetricCryptoUtils.encrypt(toCipher, dataKey, negotiationResult.getLocalIv());
    }

    /**
     * 解密数据
     */
    public static byte[] decrypt(NegotiationResult negotiationResult, byte[] dataKey, byte[] cipherText) throws SymmetricCryptoException {
        return SymmetricCryptoUtils.decrypt(cipherText, dataKey, negotiationResult.getLocalIv());
    }

    /**
     * 创建一个协商请求
     */
    public NegotiationRequest createRequest() throws AsymmetricCryptoException {
        NegotiationRequest request = new NegotiationRequest();
        // 生成会话唯一标识
        String clientSessionId = UUID.randomUUID().toString().replaceAll("-", "");
        request.setxSessionId(clientSessionId);
        asymmetricCryptoProcessor.buildKeyPair(clientSessionId, negotiationDuration);
        request.setPublicKey(ByteSpecification.encodeToString(asymmetricCryptoProcessor.getPublicKey(clientSessionId).getEncoded()));
        request.setRefresh(false);
        request.setToken(ByteSpecification.encodeToString(generateRequestToken(request)));
        return request;
    }

    /**
     * 协商密钥交换响应
     * 主要是获得密钥与 iv
     */
    public NegotiationResult negotiation(NegotiationResponse negotiationResponse) throws KeyPairException, NegotiationException {
        byte[] selfPrivateKey = asymmetricCryptoProcessor.getPrivateKey(negotiationResponse.getxSessionId()).getEncoded();
        byte[] otherPublicKey = ByteSpecification.decodeToBytes(negotiationResponse.getPublicKey());
        List<byte[]> keyAndIv = ECDHUtils.negotiationToKeyAndIv(selfPrivateKey, otherPublicKey, negotiationResponse.getKeyBytesLength());

        NegotiationResult result = new NegotiationResult();
        result.setShareKey(keyAndIv.get(0));
        result.setLocalIv(keyAndIv.get(1));
        result.setPublicKey(otherPublicKey);
        result.setxSessionId(negotiationResponse.getxSessionId());
        result.setKeyLength(negotiationResponse.getKeyBytesLength());

        long expireTimePoint = System.currentTimeMillis() + negotiationResponse.getExpireTime();
        result.setExpireTime(expireTimePoint);
        return result;
    }

    /**
     * 根据协商请求准备协商参数：确定加密算法、密钥长度、协商有效期
     */
    public NegotiationResponse prepareNegotiation(NegotiationRequest negotiationRequest) throws AsymmetricCryptoException {
        // 这时候还没有缓存，因此需要生成
        String xSessionId = negotiationRequest.getxSessionId();
        asymmetricCryptoProcessor.buildKeyPair(xSessionId, negotiationDuration);
        byte[] selfPublicKey = asymmetricCryptoProcessor.getPublicKey(xSessionId).getEncoded();

        NegotiationResponse response = new NegotiationResponse();

        final int keyByteLength = randomKeyLength();
        response.setEncryptionScheme(String.valueOf(keyByteLength));
        response.setKeyBytesLength(keyByteLength);
        response.setExpireTime(NegotiationConstants.EXPIRE_TIME);
        response.setPublicKey(ByteSpecification.encodeToString(selfPublicKey));
        // todo 根据协商请求，选择自己支持的算法
        response.setEncryptionScheme("AES-" + (keyByteLength << 3));
        response.setxSessionId(xSessionId);
        response.setToken(ByteSpecification.encodeToString(generateResponseToken(response)));

        return response;
    }


    // =========================== 防篡改 ==================================

    /**
     * 发起协商请求时需要签名的数据
     *
     * @param request 协商请求
     * @return 需要签名的数据
     */
    private byte[] getNeedToSign(NegotiationRequest request) {
        byte[] xSessionIdBytes = request.getxSessionId().getBytes(ByteSpecification.STD_CHAR_SET);
        byte[] publicKeyBytes = ByteSpecification.decodeToBytes(request.getPublicKey());
        return ByteUtils.compound(Arrays.asList(xSessionIdBytes, publicKeyBytes));
    }

    /**
     * 生成 token（发起协商请求时）
     */
    public byte[] generateRequestToken(NegotiationRequest request) throws AsymmetricCryptoException {
        return asymmetricCryptoProcessor.sign(request.getxSessionId(), getNeedToSign(request));
    }

    /**
     * 验签，防篡改（处理协商请求时）
     */
    public boolean verifyRequestToken(NegotiationRequest request) throws AsymmetricCryptoException {
        byte[] signature = ByteSpecification.decodeToBytes(request.getToken());
        return asymmetricCryptoProcessor.verify(
            ByteSpecification.decodeToBytes(request.getPublicKey()),
            getNeedToSign(request),
            signature
        );
    }

    // -------------------------------

    /**
     * 响应需要签名的数据
     *
     * @param response 响应
     * @return 需要签名的数据
     */
    private byte[] getNeedToSign(NegotiationResponse response) {
        byte[] xSessionIdBytes = response.getxSessionId().getBytes(ByteSpecification.STD_CHAR_SET);
        byte[] publicKeyBytes = ByteSpecification.decodeToBytes(response.getPublicKey());
        byte[] aesBytes = response.getEncryptionScheme().getBytes(ByteSpecification.STD_CHAR_SET);
        byte[] aesKeyLength = ByteUtils.intToBytes(response.getKeyBytesLength());
        byte[] expireTimeBytes = ByteUtils.intToBytes(response.getExpireTime());
        return ByteUtils.compound(Arrays.asList(xSessionIdBytes, publicKeyBytes, aesBytes, aesKeyLength, expireTimeBytes));
    }

    /**
     * 生成 token（协商响应时）
     */
    public byte[] generateResponseToken(NegotiationResponse response) throws AsymmetricCryptoException {
        return asymmetricCryptoProcessor.sign(response.getxSessionId(), getNeedToSign(response));
    }

    /**
     * 验签，防篡改（确认协商响应请求时）
     */
    public boolean verifyResponseToken(NegotiationResponse response) throws AsymmetricCryptoException {
        byte[] signature = ByteSpecification.decodeToBytes(response.getToken());
        return asymmetricCryptoProcessor.verify(
            ByteSpecification.decodeToBytes(response.getPublicKey()),
            getNeedToSign(response),
            signature
        );
    }

    // -------------------------------

    /**
     * 生成 token（协商完毕，每次发送安全会话请求时）
     *
     * @param xDk 每次请求的临时密钥密文
     */
    public byte[] generateToken(String xSessionId, @Nullable byte[] xDk) throws AsymmetricCryptoException {
        byte[] xSessionIdBytes = xSessionId.getBytes(ByteSpecification.STD_CHAR_SET);
        byte[] toSin = ByteUtils.compound(Arrays.asList(xSessionIdBytes, xDk));
        return asymmetricCryptoProcessor.sign(xSessionId, toSin);
    }

    /**
     * 验签，防篡改（协商完毕，每次处理请求时）
     *
     * @param xSessionId 安全会话标识（密钥交换缓存 key )
     * @param xDk        临时数据密钥密文
     * @param signature  签名
     */
    public boolean verifyToken(String xSessionId, byte[] xDk, byte[] signature, byte[] otherPublicKey) throws AsymmetricCryptoException {
        byte[] xSessionIdBytes = xSessionId.getBytes(ByteSpecification.STD_CHAR_SET);
        byte[] toSin = ByteUtils.compound(Arrays.asList(xSessionIdBytes, xDk));
        return asymmetricCryptoProcessor.verify(otherPublicKey, toSin, signature);
    }

    /**
     * 简单随机算法，50% 128，25%192，25%256
     *
     * @return 128/192/256
     */
    private static int randomKeyLength() {
        return SUPPORT_KEY_BYTE_LENGTH[ThreadLocalRandom.current().nextInt(SUPPORT_KEY_BYTE_LENGTH.length)];
    }
}
