package org.shoulder.crypto.negotiation;

import org.shoulder.core.util.ByteUtils;

public class EccUtilTest {
    /**
     * 生成一次请求使用的数据密钥
     *
     * @param length 16/24/32
     * @return 数据密钥
     */
    private static byte[] newTempKey(int length) {
        return ByteUtils.randomBytes(length);
    }


//    public static void main(String[] args) throws Exception {
//        KeyPair severKeyPair = ECDHUtils.getKeyPair();
//        byte[] sPubKey = ByteSpecification.decodeToBytes(ECDHUtils.getPublicKey(severKeyPair));
//        byte[] sPriKey = ByteSpecification.decodeToBytes(ECDHUtils.getPrivateKey(severKeyPair));
//
//
//        KeyPair clientKeyPair = ECDHUtils.getKeyPair();
//        byte[] cPubKey = ByteSpecification.decodeToBytes(ECDHUtils.getPublicKey(clientKeyPair));
//        byte[] cPriKey = ByteSpecification.decodeToBytes(ECDHUtils.getPrivateKey(clientKeyPair));
//
//        List<byte[]> sResult = ECDHUtils.negotiationToKeyAndIv(sPriKey, cPubKey, 16);
//        List<byte[]> cResult = ECDHUtils.negotiationToKeyAndIv(cPriKey, sPubKey, 16);
//
//
//        outBytes(sResult.get(0));
//        outBytes(sResult.get(1));
//
//        outBytes(cResult.get(0));
//        outBytes(cResult.get(1));
//    }

    private static void outBytes(byte[] param) {
        for (byte b : param) {
            if (((char) b) == '-') {
                continue;
            }
            System.out.print(b);
        }
        System.out.println();
    }
}
