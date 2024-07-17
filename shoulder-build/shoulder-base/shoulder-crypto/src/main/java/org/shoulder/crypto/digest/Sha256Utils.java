package org.shoulder.crypto.digest;

import jakarta.annotation.Nonnull;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.ArrayUtils;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 通用的 SHA256
 *
 * @author lym
 */
public class Sha256Utils implements ByteSpecification {

    /**
     * 计算摘要
     *
     * @param toHashBytes 需要hash的 bytes
     * @return 结果
     */
    public static byte[] digest(@Nonnull byte[] toHashBytes) {
        AssertUtils.isTrue(ArrayUtils.isNotEmpty(toHashBytes), CommonErrorCodeEnum.ILLEGAL_PARAM, "text can't be empty!");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(toHashBytes);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException SHA-256", e);
        }
    }

    /**
     * 计算摘要，并转为 base64 str
     *
     * @param toHashStr 需要hash的 str
     * @return 结果
     */
    public static String digest(@Nonnull String toHashStr) {
        return ByteSpecification.encodeToString(digest(toHashStr.getBytes(ByteSpecification.STD_CHAR_SET)));
    }

    /**
     * 先计算摘要，再转为 hex 格式 str
     *
     * @param toHashStr 需要hash的 str
     * @return 结果
     * @deprecated hex 暂无合适使用场景，建议优先使用  #digest(
     */
//    public static String digestToHex(String toHashStr) {
//        byte[] cipher = digest(toHashStr.getBytes(ByteSpecification.STD_CHAR_SET));
//        return byte2Hex(cipher);
//    }

    /**
     * 将byte转为16进制
     *
     * @param bytes bytes
     * @return 十六进制 byte 的 Str
     */
//    private static String byte2Hex(byte[] bytes) {
//        StringBuilder stringBuffer = new StringBuilder();
//        String temp;
//        for (byte aByte : bytes) {
//            temp = Integer.toHexString(aByte & 0xFF);
//            if (temp.length() == 1) {
//                //1得到一位的进行补0操作
//                stringBuffer.append("0");
//            }
//            stringBuffer.append(temp);
//        }
//        return stringBuffer.toString();
//    }

    /**
     * 验证原文hash后是否与密文相同
     *
     * @param text        cipher的明文
     * @param digestBytes text hash后的摘要内容
     */
    public static boolean verify(byte[] text, byte[] digestBytes) {
        return Arrays.equals(digest(text), digestBytes);
    }

    /**
     * 验证原文hash后是否与密文相同
     *
     * @param text   cipher的明文
     * @param digest text hash后的摘要内容
     */
    public static boolean verify(String text, String digest) {
        return StringUtils.equals(digest(text), digest);
    }

}
