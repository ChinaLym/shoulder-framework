package org.shoulder.crypto.negotiation.util;

import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.crypto.aes.AesUtil;
import org.shoulder.crypto.aes.exception.AesCryptoException;
import org.shoulder.crypto.aes.exception.SymmetricCryptoException;
import org.shoulder.crypto.asymmetric.annotation.Ecc;
import org.shoulder.crypto.asymmetric.exception.AsymmetricCryptoException;
import org.shoulder.crypto.asymmetric.exception.KeyPairException;
import org.shoulder.crypto.asymmetric.processor.AsymmetricCryptoProcessor;
import org.shoulder.crypto.negotiation.ECDHUtils;
import org.shoulder.crypto.negotiation.constant.KeyExchangeConstants;
import org.shoulder.crypto.negotiation.dto.KeyExchangeResult;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.shoulder.crypto.negotiation.support.dto.KeyExchangeRequest;
import org.shoulder.crypto.negotiation.support.dto.KeyExchangeResponse;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
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

    private final AsymmetricCryptoProcessor eccProcessor;

    private final Duration negotiationDuration = Duration.ofHours(1);

    public TransportCryptoByteUtil(@Ecc AsymmetricCryptoProcessor eccProcessor) {
        this.eccProcessor = eccProcessor;
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
    public static byte[] encryptDk(KeyExchangeResult keyExchangeResult, byte[] dataKey) throws AesCryptoException {
        return AesUtil.encrypt(dataKey, keyExchangeResult.getLocalKey(), keyExchangeResult.getLocalIv());
    }

    /**
     * 解密 xDk（用于协商完毕，每次请求中）
     *
     * @return dataKey
     */
    public static byte[] decryptDk(KeyExchangeResult keyExchangeResult, byte[] xDk) throws SymmetricCryptoException {
        return AesUtil.decrypt(xDk, keyExchangeResult.getLocalKey(), keyExchangeResult.getLocalIv());
    }

    /**
     * 加密数据
     */
    public static byte[] encrypt(KeyExchangeResult keyExchangeResult, byte[] dataKey, byte[] toCipher) throws AesCryptoException {
        return AesUtil.encrypt(toCipher, dataKey, keyExchangeResult.getLocalIv());
    }

    /**
     * 解密数据
     */
    public static byte[] decrypt(KeyExchangeResult keyExchangeResult, byte[] dataKey, byte[] cipherText) throws AesCryptoException {
        return AesUtil.decrypt(cipherText, dataKey, keyExchangeResult.getLocalIv());
    }

    /**
     * 创建一个协商请求
     */
    public KeyExchangeRequest createRequest() throws AsymmetricCryptoException {
        KeyExchangeRequest request = new KeyExchangeRequest();
        // 生成会话唯一标识
        String clientSessionId = UUID.randomUUID().toString().replaceAll("-", "");
        request.setxSessionId(clientSessionId);
        eccProcessor.buildKeyPair(clientSessionId, negotiationDuration);
        request.setPublicKey(ByteSpecification.encodeToString(eccProcessor.getPublicKey(clientSessionId).getEncoded()));
        request.setRefresh(false);
        request.setToken(ByteSpecification.encodeToString(generateRequestToken(request)));
        return request;
    }

    /**
     * 协商密钥交换响应
     * 主要是获得密钥与 iv
     */
    public KeyExchangeResult negotiation(KeyExchangeResponse keyExchangeResponse) throws KeyPairException, NegotiationException {
        byte[] selfPrivateKey = eccProcessor.getPrivateKey(keyExchangeResponse.getxSessionId()).getEncoded();
        byte[] otherPublicKey = ByteSpecification.decodeToBytes(keyExchangeResponse.getPublicKey());
        List<byte[]> keyAndIv = ECDHUtils.negotiationToKeyAndIv(selfPrivateKey, otherPublicKey, keyExchangeResponse.getKeyBytesLength());

        KeyExchangeResult result = new KeyExchangeResult();
        result.setLocalKey(keyAndIv.get(0));
        result.setLocalIv(keyAndIv.get(1));
        result.setPublicKey(otherPublicKey);
        result.setxSessionId(keyExchangeResponse.getxSessionId());
        result.setKeyLength(keyExchangeResponse.getKeyBytesLength());
        long expireTimePoint = System.currentTimeMillis() + keyExchangeResponse.getExpireTime();
        result.setExpireTime(expireTimePoint);
        return result;
    }

    /**
     * 根据协商请求准备协商参数：确定加密算法、密钥长度、协商有效期
     */
    public KeyExchangeResponse prepareNegotiation(KeyExchangeRequest keyExchangeRequest) throws AsymmetricCryptoException {
        // 这时候还没有缓存，因此需要生成
        String xSessionId = keyExchangeRequest.getxSessionId();
        eccProcessor.buildKeyPair(xSessionId, negotiationDuration);
        byte[] selfPublicKey = eccProcessor.getPublicKey(xSessionId).getEncoded();

        KeyExchangeResponse response = new KeyExchangeResponse();

        final int keyByteLength = randomKeyLength();
        response.setAes(String.valueOf(keyByteLength));
        response.setKeyBytesLength(keyByteLength);
        response.setExpireTime(KeyExchangeConstants.EXPIRE_TIME);
        response.setPublicKey(ByteSpecification.encodeToString(selfPublicKey));

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
    private byte[] getNeedToSign(KeyExchangeRequest request) {
        byte[] xSessionIdBytes = request.getxSessionId().getBytes(ByteSpecification.STD_CHAR_SET);
        byte[] publicKeyBytes = ByteSpecification.decodeToBytes(request.getPublicKey());
        return ByteUtils.compound(Arrays.asList(xSessionIdBytes, publicKeyBytes));
    }

    /**
     * 生成 token（发起协商请求时）
     */
    public byte[] generateRequestToken(KeyExchangeRequest request) throws AsymmetricCryptoException {
        return eccProcessor.sign(request.getxSessionId(), getNeedToSign(request));
    }

    /**
     * 验签，防篡改（处理协商请求时）
     */
    public boolean verifyRequestToken(KeyExchangeRequest request) throws AsymmetricCryptoException {
        byte[] signature = ByteSpecification.decodeToBytes(request.getToken());
        return eccProcessor.verify(
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
    private byte[] getNeedToSign(KeyExchangeResponse response) {
        byte[] xSessionIdBytes = response.getxSessionId().getBytes(ByteSpecification.STD_CHAR_SET);
        byte[] publicKeyBytes = ByteSpecification.decodeToBytes(response.getPublicKey());
        byte[] aesBytes = response.getAes().getBytes(ByteSpecification.STD_CHAR_SET);
        byte[] aesKeyLength = ByteUtils.intToBytes(response.getKeyBytesLength());
        byte[] expireTimeBytes = ByteUtils.intToBytes(response.getExpireTime());
        return ByteUtils.compound(Arrays.asList(xSessionIdBytes, publicKeyBytes, aesBytes, aesKeyLength, expireTimeBytes));
    }

    /**
     * 生成 token（协商响应时）
     */
    public byte[] generateResponseToken(KeyExchangeResponse response) throws AsymmetricCryptoException {
        return eccProcessor.sign(response.getxSessionId(), getNeedToSign(response));
    }

    /**
     * 验签，防篡改（确认协商响应请求时）
     */
    public boolean verifyResponseToken(KeyExchangeResponse response) throws AsymmetricCryptoException {
        byte[] signature = ByteSpecification.decodeToBytes(response.getToken());
        return eccProcessor.verify(
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
        return eccProcessor.sign(xSessionId, toSin);
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
        return eccProcessor.verify(otherPublicKey, toSin, signature);
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
