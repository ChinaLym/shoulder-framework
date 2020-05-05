package org.shoulder.core.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密编码规范
 * @author lym
 */
public interface ByteSpecification {
    /** 统一字符编码 */
    Charset CHARSET_UTF_8 = StandardCharsets.UTF_8;

    /** 统一比特编码 */
    static String encodeToString(byte[] bytes){
        return new String(Base64.getEncoder().encode(bytes), CHARSET_UTF_8);
    }

    /** 统一比特解码 */
    static byte[] decodeToBytes(String base64String){
        return Base64.getDecoder().decode(base64String);
    }
}
