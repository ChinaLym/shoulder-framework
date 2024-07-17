package org.shoulder.crypto.negotiation.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.crypto.digest.Sha256Utils;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyAgreement;

/**
 * ECDH 密钥谈判工具
 *
 * @author lym
 */
@SuppressWarnings("PMD.ClassNamingShouldBeCamelRule")
public class ECDHUtils {

    private final static String PROVIDER = "BC";

    private final static String ECDH = "ECDH";

    private static final Logger logger = ShoulderLoggers.SHOULDER_CRYPTO;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 根据己方私钥 + 对方公钥得出 shareKey、shareIv
     *
     * @param selfPrivateKey 己方的私钥
     * @param otherPublicKey 对方的公钥
     * @param keyBytes       aes密钥长度除8：16/24/32
     * @return keyAndIv                 sessionAesKey, sessionAesIv
     * @throws NegotiationException 密钥协商出错
     */
    public static List<byte[]> negotiationToKeyAndIv(byte[] selfPrivateKey, byte[] otherPublicKey, int keyBytes) throws NegotiationException {
        try {
            // 初始化ecdh keyFactory
            KeyFactory keyFactory = KeyFactory.getInstance(ECDH, PROVIDER);
            // 处理私钥
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(selfPrivateKey);
            PrivateKey ecPriKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            // 处理公钥
            X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(otherPublicKey);
            PublicKey ecPubKey = keyFactory.generatePublic(pubX509);
            // 密钥协商生成新的密钥byte数组
            KeyAgreement aKeyAgree = KeyAgreement.getInstance(ECDH, PROVIDER);
            aKeyAgree.init(ecPriKey);
            aKeyAgree.doPhase(ecPubKey, true);
            return generateLocalKeyAndIv(aKeyAgree.generateSecret(), keyBytes);
        } catch (Exception e) {
            logger.error("密钥协商出现异常", e);
            throw new NegotiationException("密钥协商出现异常", e);
        }
    }

    /**
     * 将 negotiationKey SHA256 -> 256bit，取前 keyLength 作为共享密钥，取后 16 bit作为 iv
     * 由于双方 negotiationKey、keyLength 一样，故结果一样
     *
     * @param negotiationKey 协商出来的密钥
     * @param keyBytes       共享密钥长度（字节数，非bit数）
     * @return 共享密钥、init-vector
     */
    private static List<byte[]> generateLocalKeyAndIv(byte[] negotiationKey, int keyBytes) {
        Assert.notNull(negotiationKey, "negotiationKey can't be null!");
        Assert.isTrue(negotiationKey.length == 32, "ECDH256 negotiationKey.length must be 32(256bit)!");
        byte[] temp = Sha256Utils.digest(negotiationKey);
        byte[] localKey = new byte[keyBytes];
        System.arraycopy(temp, 0, localKey, 0, keyBytes);

        final int ivLength = 16;
        byte[] localIv = new byte[ivLength];
        System.arraycopy(temp, temp.length - 16, localIv, 0, ivLength);

        List<byte[]> keyAndIv = new ArrayList<>(2);
        keyAndIv.add(localKey);
        keyAndIv.add(localIv);

        return keyAndIv;

    }

}
