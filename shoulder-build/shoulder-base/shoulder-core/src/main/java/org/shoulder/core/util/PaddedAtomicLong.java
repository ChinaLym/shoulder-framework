package org.shoulder.core.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 在 AtomicLong 后添加了 CPU 缓存行，适用于缓存行大小为 64bytes 的 CPU 上
 *
 * @author lym
 */
public class PaddedAtomicLong extends AtomicLong {

    /**
     * 64 位操作系统，对象头有 8bytes（64 bit）；AtomicLong.value 8bytes。再补充6个 long(48bytes) 即可占满 cacheLine
     */
    public volatile long p1, p2, p3, p4, p5, p6 = 7L;

    public PaddedAtomicLong() {
        super();
    }

    public PaddedAtomicLong(long initialValue) {
        super(initialValue);
    }

    /**
     * 防止编译器优化掉填充字段（jdk7后会自动优化无用字段）
     */
    public long preventOptimization() {
        return p1 + p2 + p3 + p4 + p5 + p6;
    }

}
