package org.shoulder.crypto.digest;

import org.shoulder.core.constant.ByteSpecification;

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
     * 获取摘要
     */
    public static byte[] digest(byte[] toHashBytes) {
        if (toHashBytes == null || toHashBytes.length == 0) {
            throw new IllegalArgumentException("text can't be empty!");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(toHashBytes);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("NoSuchAlgorithmException SHA-256", e);
        }
    }

    public static String digest(String toHashStr) {
        return ByteSpecification.encodeToString(digest(toHashStr.getBytes(ByteSpecification.STD_CHAR_SET)));
    }

    /**
     * sha256之后转为十六进制的String
     **/
    public static String digestToHex(String toHashStr) {
        byte[] cipher = digest(toHashStr.getBytes(ByteSpecification.STD_CHAR_SET));
        return byte2Hex(cipher);
    }

    /**
     * 将byte转为16进制
     **/
    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * 验证原文hash后是否与密文相同
     *
     * @param text        cipher的明文
     * @param digestBytes text hash厚的
     */
    public static boolean verify(byte[] text, byte[] digestBytes) {
        return Arrays.equals(digest(text), digestBytes);
    }

    /**
     * 验证原文hash后是否与密文相同
     *
     * @param text   cipher的明文
     * @param cipher text hash厚的
     */
    public static boolean verify(String text, String cipher) {
        return verify(text.getBytes(ByteSpecification.STD_CHAR_SET), ByteSpecification.decodeToBytes(cipher));
    }

}
