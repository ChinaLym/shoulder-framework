package org.shoulder.crypto.sign;

import org.shoulder.core.constant.ByteSpecification;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

/**
 * HmacSha256
 * Hash-based Message Authentication Code
 *
 * @author lym
 */
public class HmacSha256Util {

    private static final Charset CHAR_SET = ByteSpecification.STD_CHAR_SET;

    private static final String HMAC_SHA256 = "HmacSHA256";

    public static String digest(String key, String toDigest) throws Exception {

        Mac hmacSha256 = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(CHAR_SET), HMAC_SHA256);
        hmacSha256.init(secretKey);
        byte[] array = hmacSha256.doFinal(toDigest.getBytes(CHAR_SET));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }

}
