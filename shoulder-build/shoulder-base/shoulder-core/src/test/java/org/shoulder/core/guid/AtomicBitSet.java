package org.shoulder.core.guid;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * 并发时，jdk提供的 BitSet 是线程不安全的，为了保证测试的准确性，自行实现 AtomicBitSet
 *
 * @author lym
 * @deprecated 写时直接使用 synchronized
 */
public class AtomicBitSet {

    private final AtomicIntegerArray array;

    public AtomicBitSet(int length) {
        int intLength = (length + 31) / 32;
        array = new AtomicIntegerArray(intLength);
    }

    public void set(long n) {
        int bit = 1 << n;
        int idx = (int) (n >>> 5);
        while (true) {
            int num = array.get(idx);
            int num2 = num | bit;
            if (num == num2 || array.compareAndSet(idx, num, num2))
                return;
        }
    }

    public boolean get(long n) {
        int bit = 1 << n;
        int idx = (int) (n >>> 5);
        int num = array.get(idx);
        return (num & bit) != 0;
    }

    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < array.length(); i++)
            sum += Long.bitCount(array.get(i));
        return sum;
    }

}
