package org.shoulder.crypto.negotiation;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shoulder.core.util.ByteUtils;
import org.shoulder.crypto.digest.Sha256Utils;
import org.shoulder.crypto.negotiation.exception.NegotiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.crypto.KeyAgreement;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * ECDH 密钥谈判工具
 *
 * @author lym
 */
public class ECDHUtils {

    private final static String PROVIDER = "BC";
    private final static String ECDH = "ECDH";
    private static Logger logger = LoggerFactory.getLogger(ECDHUtils.class);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * @param selfPrivateKey 己方的私钥
     * @param otherPublicKey 对方的公钥
     * @param keyLength      aes密钥长度除8：16/24/32
     * @return keyAndIv                 sessionAesKey, sessionAesIv
     * @throws NegotiationException 密钥协商出错
     */
    public static List<byte[]> negotiationToKeyAndIv(byte[] selfPrivateKey, byte[] otherPublicKey, int keyLength) throws NegotiationException {
        try {
            //初始化ecdh keyFactory
            KeyFactory keyFactory = KeyFactory.getInstance(ECDH, PROVIDER);
            //处理私钥
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(selfPrivateKey);
            PrivateKey ecPriKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            //处理公钥
            X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(otherPublicKey);
            PublicKey ecPubKey = keyFactory.generatePublic(pubX509);
            //秘钥磋商生成新的秘钥byte数组
            KeyAgreement aKeyAgree = KeyAgreement.getInstance(ECDH, PROVIDER);
            aKeyAgree.init(ecPriKey);
            aKeyAgree.doPhase(ecPubKey, true);
            return generateLocalKeyAndIv(aKeyAgree.generateSecret(), keyLength);
        } catch (Exception e) {
            logger.error("秘钥磋商出现异常", e);
            throw new NegotiationException("秘钥磋商出现异常", e);
        }
    }

    private static List<byte[]> generateLocalKeyAndIv(byte[] negotiationKey, int keyLength) {
        Assert.notNull(negotiationKey, "negotiationKey can't be null!");
        Assert.isTrue(negotiationKey.length == 32, "ECDH256 negotiationKey.length must be 32(256bit)!");
        byte[] temp = Sha256Utils.digest(negotiationKey);

        byte[] localKey = new byte[keyLength];
        System.arraycopy(temp, 0, localKey, 0, keyLength);

        final int ivLength = 16;
        byte[] localIv = new byte[ivLength];
        System.arraycopy(temp, temp.length - 16, localIv, 0, ivLength);

        List<byte[]> keyAndIv = new ArrayList<>(2);
        keyAndIv.add(localKey);
        keyAndIv.add(localIv);

        return keyAndIv;

    }

    /**
     * 生成一次请求使用的数据密钥
     *
     * @param length 16/24/32
     * @return 数据秘钥
     */
    private static byte[] newTempKey(int length) {
        return ByteUtils.randomBytes(length);
    }


    /*public static void main(String[] args) throws Exception {
        KeyPair severKeyPair = EccUtil.getKeyPair();
        byte[] sPubKey = ByteSpecification.decodeToBytes(EccUtil.getPublicKey(severKeyPair));
        byte[] sPriKey = ByteSpecification.decodeToBytes(EccUtil.getPrivateKey(severKeyPair));


        KeyPair clientKeyPair = EccUtil.getKeyPair();
        byte[] cPubKey = ByteSpecification.decodeToBytes(EccUtil.getPublicKey(clientKeyPair));
        byte[] cPriKey = ByteSpecification.decodeToBytes(EccUtil.getPrivateKey(clientKeyPair));

        List<byte[]> sResult = negotiationToKeyAndIv(sPriKey, cPubKey, 16);
        List<byte[]> cResult = negotiationToKeyAndIv(cPriKey, sPubKey, 16);


        outBytes(sResult.get(0));
        outBytes(sResult.get(1));

        outBytes(cResult.get(0));
        outBytes(cResult.get(1));
    }

    private static void outBytes(byte[] param) {
        for (byte b : param) {
            if (((char) b) == '-') {
                continue;
            }
            System.out.print(b);
        }
        System.out.println();
    }*/
}
