package org.shoulder.core.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;


/**
 * byte 相关工具类
 *
 * @author lym
 */
public class ByteUtils {

    /**
     * 生成 length 位随机 bit
     *
     * @param length 随机位数
     * @return length 位随机 bit
     */
    public static byte[] randomBytes(int length) {
        byte[] safeRandom = new byte[length];
        new SecureRandom().nextBytes(safeRandom);
        return safeRandom;
    }

    /**
     * 复制数组中某一部分
     *
     * @return 选择的部分
     */
    public static byte[] copy(byte[] source, int index, int limit) {
        byte[] cloneBytes = new byte[limit];
        System.arraycopy(source, index, cloneBytes, 0, limit);
        return cloneBytes;
    }

    /**
     * byte 数组转 16 进制字符串
     *
     * @param bArr byte Array
     * @return 16 进制字符串
     */
    public String toHexString(byte[] bArr) {
        StringBuilder sb = new StringBuilder(bArr.length);
        String sTmp;
        for (byte b : bArr) {
            sTmp = Integer.toHexString(0xFF & b);
            if (sTmp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTmp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制字符串转换为byte数组
     *
     * @param hexStr 16进制字符串
     * @return bytes
     */
    public static byte[] hexStringToBytes(String hexStr) throws DecoderException {
        return Hex.decodeHex(hexStr);
    }

    /**
     * 带注释的 {@link System#arraycopy}
     */
    public static void copy(byte[] source, int from, byte[] destination, int index, int limit) {
        System.arraycopy(source, from, destination, index, limit);
    }

    /**
     * 把源数组内容拷贝到目标数组的 index
     * 如 source = [1,2,3,4]， destination = new byte[]{0,0,0,0,0,0,0,0,0,0}
     * 经过 copy(source, destination, 1)
     * 结果：destination: [0,1,2,3,4,0,0,0,0,0]
     */
    public static void copy(byte[] source, byte[] destination, int index) {
        int limit = Integer.min(source.length, destination.length - index);
        System.arraycopy(source, 0, destination, index, limit);
    }

    /**
     * 将会多个数组拼接为一个数组
     */
    public static byte[] compound(List<byte[]> bytes) {
        if (bytes == null || bytes.isEmpty()) {
            return null;
        }
        List<byte[]> nonNullList = bytes.stream().filter(Objects::nonNull).toList();
        int count = nonNullList.stream().map(item -> item.length).reduce(0, Integer::sum);

        byte[] result = new byte[count];

        int length = 0;
        for (byte[] blockBytes : nonNullList) {
            copy(blockBytes, 0, result, length, blockBytes.length);
            length += blockBytes.length;
        }
        return result;
    }

    /**
     * int到byte[] 由高位到低位
     *  相当于以下（性能无差异）：
     byte[] result = new byte[4];
     result[0] = (byte) ((i >> 24) & 0xFF);
     result[1] = (byte) ((i >> 16) & 0xFF);
     result[2] = (byte) ((i >> 8) & 0xFF);
     result[3] = (byte) (i & 0xFF);
     return result;
     *
     * @param input 需要转换为byte数组的值。
     * @return byte数组
     */
    public static byte[] toBytes(int input) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        byteBuffer.putInt(0, input);
        return byteBuffer.array();
    }

    public static byte[] toBytes(long input) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
        byteBuffer.putLong(0, input);
        return byteBuffer.array();
    }

    /**
     * byte[]转int
     *
     * @param bytes 需要转换成int的数组 位运算比 byteBuffer 快很多
     * @return int值
     */
    public static int toInt(byte[] bytes) {
        int value = 0;
        value |= (bytes[0] & 0xFF) << 24;
        value |= (bytes[1] & 0xFF) << 16;
        value |= (bytes[2] & 0xFF) << 8;
        value |= bytes[3] & 0xFF;
        return value;
    }

    public static byte[] intToBytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * byte[]转 long（性能：位运算 > 缓冲区 ByteBuffer.allocate > ByteArrayInputStream、大端存储符合人类的阅读习惯）
     *
     * @param bytes 需要转换成 long 的数组
     * @return long 值
     */
    public static long toLong(byte[] bytes) {
        long value = 0;
        value |= (bytes[0] & 0xFFL) << 56;
        value |= (bytes[1] & 0xFFL) << 48;
        value |= (bytes[2] & 0xFFL) << 40;
        value |= (bytes[3] & 0xFFL) << 32;
        value |= (bytes[4] & 0xFFL) << 24;
        value |= (bytes[5] & 0xFFL) << 16;
        value |= (bytes[6] & 0xFFL) << 8;
        value |= bytes[7] & 0xFFL;
        return value;
    }

    /**
     * long 到byte[] 由高位到低位大端存储符合人类的阅读习惯，位运算计算量较多，并无优势，建议用buffer
     *
     * @param input 需要转换为byte数组的值。
     * @return byte数组
     */
//    public static byte[] toBytes2(long input) {
//        byte[] result = new byte[8];
//        result[0] = (byte) ((input >> 56) & 0xFF);
//        result[1] = (byte) ((input >> 48) & 0xFF);
//        result[2] = (byte) ((input >> 40) & 0xFF);
//        result[3] = (byte) (input >> 32 & 0xFF);
//        result[4] = (byte) (input >> 24 & 0xFF);
//        result[5] = (byte) (input >> 16 & 0xFF);
//        result[6] = (byte) (input >> 8 & 0xFF);
//        result[7] = (byte) (input & 0xFF);
//        return result;
//    }

    public static boolean isEmpty(byte[] data) {
        return data == null || data.length == 0;
    }

    public static boolean isNotEmpty(byte[] data) {
        return !isEmpty(data);
    }

}
