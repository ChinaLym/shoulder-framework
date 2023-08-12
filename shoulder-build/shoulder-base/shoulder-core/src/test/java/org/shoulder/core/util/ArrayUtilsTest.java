package org.shoulder.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author lym
 */
public class ArrayUtilsTest {


    @Test
    public void testSub() {
        Character[] chars = {'a', 'b', 'c', 'd', 'e'};
        System.out.println(Arrays.toString(chars));
        Character[] sub = ArrayUtils.subArray(chars, 1, chars.length);
        System.out.println(Arrays.toString(sub));
        System.out.println("1".substring(1));
    }

    @Test
    public void testConvert() {
        long x = 20;
        long b = ByteUtils.toLong(ByteUtils.toBytes(x));
        Assertions.assertEquals(x, b, "ByteUtils. byte[] long convert");

        int d = ByteUtils.toInt(ByteUtils.toBytes((int) x));
        Assertions.assertEquals(x, d, "ByteUtils. byte[] int convert");
    }

}
