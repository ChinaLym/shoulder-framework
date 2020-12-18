package org.shoulder.core.guid;

import java.util.Base64;
import java.util.UUID;

/**
 * 22 位 UUID 生成器
 * 性能：i7-8750h 100w次生成总耗时：
 * 去掉'-'32位比原生36位，慢 11%（0.9s -> 1.0）
 * Base64压缩22位（2.7s）—— 本类
 * 字母大小写19位（4.7s）
 *
 * @author lym
 */
public class CompressedUUIDGenerator implements StringGuidGenerator {

    /**
     * 完整 UUID 共128位 (16 byte)
     */
    private static final int UUID_LENGTH = 128 >> 3;

    /**
     * UUID 可以分为两个 long 部分，long 类型长度 64 位 (8 byte)
     */
    private static final int HALF_LENGTH = 64 >> 3;

    /**
     * byte 类型有效位数为 8
     */
    private static final int BYTE_MASK = 1 << 8;

    /**
     * base64Encoder withoutPadding instance.
     */
    private static Base64.Encoder base64Encoder = Base64.getEncoder().withoutPadding();

    /**
     * 生成 22 位压缩版 UUID： 用 base64 压缩 JDK 的 UUID
     *
     * @return 压缩版的 22位 UUID
     */
    private String compressedUUID22() {
        UUID uuid = UUID.randomUUID();
        long high = uuid.getMostSignificantBits();
        long low = uuid.getLeastSignificantBits();

        byte[] byteUuid = new byte[UUID_LENGTH];
        for (int i = 0; i < HALF_LENGTH; i++) {
            byteUuid[i] = (byte) (high >>> (HALF_LENGTH * (7 - i)) & BYTE_MASK);
            byteUuid[i + HALF_LENGTH] = (byte) (low >>> (HALF_LENGTH * (7 - i)) & BYTE_MASK);
        }

        return base64Encoder.encodeToString(byteUuid);
    }

    private static char[] BASE64 = "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ-0123456789".toCharArray();

    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        char[] chs = new char[22];
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        int high = (int) ((most >> 13) ^ (least >> 31)) & 0x3c;
        int k = chs.length - 1;
        for (int i = 0; i < 10; i++, least >>>= 6) {
            chs[k--] = BASE64[(int) (least & 0x3f)];
        }
        chs[k--] = BASE64[(int) ((least & 0x3f) | (most & 0x30))];
        most >>>= 2;
        for (int i = 0; i < 10; i++, most >>>= 6) {
            chs[k--] = BASE64[(int) (most & 0x3f)];
        }
        chs[k--] = BASE64[(int) (high | most)];
        return new String(chs);
    }

    @Override
    public String nextId() {
        return generateUUID();
    }
}
