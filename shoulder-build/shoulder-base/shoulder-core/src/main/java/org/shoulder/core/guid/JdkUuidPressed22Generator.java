package org.shoulder.core.guid;

import java.util.UUID;

/**
 * jdk uuid，并压缩为 22 位
 *
 * @author lym
 */
public class JdkUuidPressed22Generator extends JdkUuidEnhancer implements StringGuidGenerator {


    @Override
    public String nextId() {
        return generateUUID22();
    }


    // ---------------------- 以下两种方式可以认为无性能差异 --------------------------

    /**
     * 固定22位UUID
     */
    public static String generateUUID22() {
        UUID uuid = UUID.randomUUID();
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        char[] out = new char[24];
        int tmp = 0, idx = 0;
        // 循环写法
        int bit = 0, bt1 = 8, bt2 = 8;
        int mask = 0x00, offsetm = 0, offsetl = 0;

        for (; bit < 16; bit += 3, idx += 4) {
            offsetm = 64 - ((bit + 3) << 3);
            offsetl = 0;
            tmp = 0;

            if (bt1 > 3) {
                mask = (1 << 8 * 3) - 1;
            } else if (bt1 >= 0) {
                mask = (1 << 8 * bt1) - 1;
                bt2 -= 3 - bt1;
            } else {
                mask = (1 << 8 * ((bt2 > 3) ? 3 : bt2)) - 1;
                bt2 -= 3;
            }
            if (bt1 > 0) {
                bt1 -= 3;
                tmp = (int) ((offsetm < 0) ? msb : (msb >>> offsetm) & mask);
                if (bt1 < 0) {
                    tmp <<= Math.abs(offsetm);
                    mask = (1 << 8 * Math.abs(bt1)) - 1;
                }
            }
            if (offsetm < 0) {
                offsetl = 64 + offsetm;
                tmp |= ((offsetl < 0) ? lsb : (lsb >>> offsetl)) & mask;
            }

            if (bit == 15) {
                out[idx + 3] = digits[64];
                out[idx + 2] = digits[64];
                tmp <<= 4;
            } else {
                out[idx + 3] = digits[tmp & 0x3f];
                tmp >>= 6;
                out[idx + 2] = digits[tmp & 0x3f];
                tmp >>= 6;
            }
            out[idx + 1] = digits[tmp & 0x3f];
            tmp >>= 6;
            out[idx] = digits[tmp & 0x3f];
        }

        return new String(out, 0, 22);
    }

    /**
     * 21-22位UUID
     */
    public static String generateMost22UUID() {

        UUID uid = UUID.randomUUID();
        long most = uid.getMostSignificantBits();

        char[] buf = new char[22];
        int charPos = 22;
        int radix = 1 << 6;
        long mask = radix - 1;
        do {
            charPos--;
            buf[charPos] = digits[(int) (most & mask)];
            most >>>= 6;
        } while (most != 0);

        long least = uid.getLeastSignificantBits();
        do {
            charPos--;
            buf[charPos] = digits[(int) (least & mask)];
            least >>>= 6;
        } while (least != 0);
        return new String(buf, charPos, 22 - charPos);
    }

}
