package org.shoulder.core.util;

import org.junit.Test;

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

}
