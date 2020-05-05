package org.shoulder.crypto.sign;

import org.shoulder.core.constant.ByteSpecification;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

/**
 * HmacSha256
 * @author lym
 */
public class HmacSha256Util {

    private static final Charset UTF_8 = ByteSpecification.CHARSET_UTF_8;

    private static final String HMAC_SHA256 = "HmacSHA256";

    public static String digest(String toDigest, String key) throws Exception {

        Mac hmacSha256 = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(UTF_8), HMAC_SHA256);
        hmacSha256.init(secretKey);
        byte[] array = hmacSha256.doFinal(toDigest.getBytes(UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }

}
