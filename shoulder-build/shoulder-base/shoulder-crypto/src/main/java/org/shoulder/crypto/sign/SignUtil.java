package org.shoulder.crypto.sign;

import org.shoulder.core.util.ArrayUtils;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.util.ByteUtils;
import org.springframework.util.Assert;

/**
 * 签名和校验工具
 * 启动参数增加 -Dsign=ignoreStamp 则签名不校验时间戳
 * 
 * @author lym
 */
public class SignUtil {

    private static final String SPLIT = ".";
    
    /**
     * 生成签名
     * Base64(AK.时间戳.SHA256(Base64(AK).Base64(时间戳), SK))
     * @param signKey 用于签名的密钥 key
     * @param toSigns 待签名的数据
     * @return 如 待签名1.待签名2.....待签名n.时间戳.签名
     */
    public static String sign(String signKey, String... toSigns) throws Exception {
        if(signKey == null || toSigns == null){
            throw new IllegalArgumentException("arguments can't be null!");
        }
        String timeStamp = ByteSpecification.encodeToString(ByteUtils.toBytes(System.currentTimeMillis()));

        StringBuilder toDigest = new StringBuilder();
        for (String toSign : toSigns) {
            toDigest.append(toSign);
            toDigest.append(SPLIT);
        }
        String toDigestStr = toDigest.append(timeStamp).toString();
        String digest = HmacSha256Util.digest(toDigestStr, signKey);

        return toDigest + SPLIT + digest;
    }


    /**
     * 校验 数据 的签名是否正确
     * @param dataAndSignStr 签名后的数据，即通过 {@link #sign} 方法生成的串，如 待签名1.待签名2.....待签名n.时间戳.签名
     * @param effective 有效期，秒
     * @param signKey 用于签名的密钥 key
     * @param exceptData （可空）期待的数据，如果为空，则代表只校验签名，不校验数据
     */
    public static boolean verify(String dataAndSignStr, String signKey, int effective, String... exceptData) {
        try{
            boolean checkTime = effective > 0;
            String[] dataAndSign = dataAndSignStr.split("\\.");


            if(exceptData != null){
                final int total = exceptData.length;

                // 签名中的值数据个数，+2含义: 时间戳、签名
                Assert.isTrue(dataAndSign.length == total + 2, "Illegal dataAndSignStr!");
                // 验证每个数据
                for (int i = 0; i < total; i++) {
                    String dataInSign = dataAndSign[i];
                    Assert.isTrue(exceptData[i].equals(dataInSign),
                            "exceptData[" + i + "] incorrect. " +
                                    "Excepted: '"+ exceptData[i] + "', but received: '" + dataInSign + "'");
                }
            }

            // 校验时间戳有效
            final long signTimeStamp = ByteUtils.toLong(ByteSpecification.decodeToBytes(dataAndSign[dataAndSign.length - 2]));
            // 为了方便调试时候不校验时间戳，启动参数增加 -Dsign=ignoreStamp
            if(!"ignoreStamp".equals(System.getProperty("sign")) && checkTime){
                boolean timeEffective = System.currentTimeMillis() - signTimeStamp < effective;
                Assert.isTrue(timeEffective, "Illegal timeStamp!");
            }

            // 3. 签名合法
            final String signDigest = dataAndSign[dataAndSign.length - 1];
            String[] data = ArrayUtils.subArray(dataAndSign, dataAndSign.length - 2);
            String digest = sign(signKey, data);
            return digest.equals(signDigest);
            
        } catch (Exception e){
            return false;
        }
    }


    // example
    /*
    public static void main(String[] args) throws Exception {
        String key = "123456";
        String data = "123456";

        String sign = sign(key, data);
        System.out.println(sign);
        System.out.println(verify(sign, key, 0));

    }*/
    
}
