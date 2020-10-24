package org.shoulder.core.uuid;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 类 SnowFlake 算法改进
 * 注意：标准的 snowflake 并不能容忍突发事件，如批量导入1w条记录时，默认单机1s只能生成4096个；
 * 不能处理时钟回拨；
 * 不能处理达到上限；
 * 不能很好的适应各种场景；
 * Shoulder 在解决了它的痛处基础上，进行了无锁化实现，支持配置每段位数、元时间戳。
 *
 * @author lym
 */
public class ShoulderGuidGenerator implements LongGuidGenerator {

    // ---------------------- Fields -------------------------

    /**
     * 实例标识 左移位数，空出自增序列所占位
     */
    private final long instanceIdShift;

    /**
     * 时间截 左移位数，空出自增序列所占位
     */
    private final long timestampLeftShift;

    /**
     * 生成序列的掩码，保证序列安全生成，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask;

    /**
     * 元时间截
     */
    private final long timeEpoch;

    /**
     * 当前服务实例标识
     */
    private long instanceId;

    /**
     * 最新的时间戳，位置，这两个要保证一起操作
     */
    private volatile long latestTimeStamp;
    private volatile int latestIndex;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private final int bufferSize;

    /**
     * 每个 Node 存储了一个时间周期的数据（默认1ms）
     */
    private final Node[] buffer;

    private final VarHandle nodeHandle = MethodHandles.arrayElementVarHandle(Node[].class);

    // ---------------------- 构造器 -------------------------

    {
        // todo 默认 1024，支持配置
        bufferSize = 1024;
        // 初始化缓冲区
        buffer = new Node[bufferSize];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = new Node(-1);
        }
    }

    /**
     * 初始化 id 自增器格式
     *
     * @param timeStampBits  时间戳所占位数 > 0
     * @param timeEpoch      元时间戳（位数必须与 timeStampBits 一致）
     * @param instanceIdBits 应用实例标识（机器标识）位数 >= 0 todo 可以自动推断
     * @param instanceId     实例标识
     * @param sequenceBits   单位时间（时间戳相关）内自增序列bit位数 > 0
     */
    public ShoulderGuidGenerator(long timeStampBits, long timeEpoch,
                                 long instanceIdBits, long instanceId,
                                 long sequenceBits) {
        this.timeEpoch = timeEpoch;
        this.instanceId = instanceId;

        // 初始化与校验
        instanceIdShift = sequenceBits;
        timestampLeftShift = sequenceBits + instanceIdBits;
        sequenceMask = ~(-1L << sequenceBits);

        // 时间戳 所占位数必须大于零，且给定元时间戳也需要符合该限制并 <= timeStampBits
        int timeEpochBits = 64 - Long.numberOfLeadingZeros(timeEpoch);
        if (timeStampBits <= 0 || timeStampBits < timeEpochBits) {
            throw new IllegalArgumentException("timeStampBits or timeEpoch invalid. " +
                "timeStampBits=" + timeStampBits + ", timeEpoch=" + timeEpoch);
        }

        // 实例标识 所占位数必须大于零，且给定实例标识也需要符合该限制并 <= instanceIdBits
        int selfInstanceIdBits = 64 - Long.numberOfLeadingZeros(instanceId);
        if (instanceIdBits < 0 || timeStampBits < selfInstanceIdBits) {
            throw new IllegalArgumentException("instanceIdBits or instanceId invalid. " +
                "instanceIdBits=" + instanceIdBits + ", instanceId=" + instanceId);
        }

        // 序列号所占位数必须大于 0
        if (sequenceBits <= 0) {
            throw new IllegalArgumentException("sequenceBits must > 0. sequenceBits=" + sequenceBits);
        }

        // 序列号所占位数必须大于 0
        if (timeStampBits + instanceIdBits + sequenceBits != 63) {
            throw new IllegalArgumentException("timeStampBits + instanceIdBits + sequenceBits != 63 must = 63. " +
                "timeStampBits=" + timeStampBits + "instanceIdBits=" + instanceIdBits + "sequenceBits=" + sequenceBits);
        }
    }

    // ---------------------- 核心实现 -------------------------

    @Override
    public long nextId() {
        long timeStamp = currentTimeStamp();
        lock.readLock().lock();
        final long lts = latestTimeStamp;
        final long lIndex = latestIndex;
        lock.readLock().unlock();

        // 缓存中最旧的时间戳
        final long minTs = lts - bufferSize;
        if (timeStamp < minTs) {
            // 时钟回拨过多（极其少见）
            onTimeBackTooMuch(timeStamp, latestTimeStamp, minTs - timeStamp);
        }

        // 尝试拿 Node
        while (true) {
            int index = ((int) (lIndex + lts - timeStamp)) % bufferSize;
            index = index < 0 ? index + bufferSize : index;
            Node node = getNodeAt(index);
            if (node.timeStamp != timeStamp) {
                // 该时间戳内第一次访问，new 新的 CAS 替换
                Node newNode = new Node(timeStamp);
                if (casNodeAt(index, node, newNode)) {
                    // 成功时序列为 0
                    return ((timeStamp - timeEpoch) << timestampLeftShift)
                        | (instanceId << instanceIdShift);
                }
                // 失败则循环
            } else {
                // timeStamp == timeStamp，该毫秒内已经有其他线程已经设置过了
                long currentSequence = node.sequence.incrementAndGet() & sequenceMask;
                if (currentSequence != 0) {
                    return ((timeStamp - timeEpoch) << timestampLeftShift)
                        | (instanceId << instanceIdShift) | currentSequence;
                }
                // 超出单毫秒内最大限制，说明该毫秒内竞争非常激烈，使用下一毫秒的数据
                timeStamp = timeStamp + 1;
            }
        }

    }

    @Override
    public Map<String, String> decode(long snowflakeId) {
        Map<String, String> map = new HashMap<>(3);
        long originTimestamp = (snowflakeId >> timestampLeftShift) + timeEpoch;
        map.put("timestamp", String.valueOf(originTimestamp));

        long sequence = snowflakeId & sequenceMask;
        map.put("sequence", String.valueOf(sequence));

        long instanceId = snowflakeId >> instanceIdShift & ~(-1 << (timestampLeftShift - instanceIdShift));
        map.put("instanceId", String.valueOf(instanceId));
        return map;
    }


    /*@Override
    public long[] nextIds(int num) {
        // todo 默认实现是循环调单个处理，但这里可以通过 getAndSet 指令一次获取多个
        return new long[0];
    }*/


    /**
     * 返回当前时间戳
     * 默认为当前毫秒数，共 41 位。如果访问量很大 / 时间位数较少 可以对其进行 lazy 化缓存提高运行效率
     *
     * @return 当前时间戳
     */
    protected long currentTimeStamp() {
        return System.currentTimeMillis();
    }


    /**
     * 时间回拨幅度过大
     *
     * @param timeStamp       本次使用的时间戳
     * @param latestTimeStamp 本生成器记录的回拨前最晚时间
     * @param minDuration     最少等待时间
     */
    protected void onTimeBackTooMuch(long timeStamp, long latestTimeStamp, long minDuration) {
        throw new IllegalStateException("Clock moved backwards too much. cover in " + minDuration + "ms.");
    }

    private Node getNodeAt(int index) {
        // 获取 buffer 数组的第 index 个元素
        return (Node) nodeHandle.get(buffer, index);
    }

    private boolean casNodeAt(int index, Node old, Node newValue) {
        // 如果 buffer 第 index 个元素是 old，则该元素被设为 newValue
        return nodeHandle.compareAndSet(buffer, index, old, newValue);
    }

    public synchronized void nextStep(int step) {
        lock.writeLock().lock();
        latestTimeStamp += step;
        latestIndex += step;
        latestIndex = latestIndex % bufferSize;
        lock.writeLock().unlock();
    }


    /**
     * 缓存区，节点，保存时间/自增序列
     * 更新方式：读写锁 （固定个数锁变量，需处理极小概率事件 ‘时钟回拨临近缓冲上限’）
     * cas Node 方式（无锁，new 小对象）【默认】
     */
    public static class Node {

        /**
         *
         */
        final long timeStamp;

        /**
         * ((timeStamp - timeEpoch) << timestampLeftShift) | (instanceId << instanceIdShift)
         * 顺便拿出8 bytes利用一下缓存行（L1 cache 快于本次计算）
         */
        final long template = -1L;

        // 干掉内存伪共享
        long padding3;
        long padding4;
        long padding5;
        long padding6;

        // 并发竞争点
        final AtomicLong sequence = new AtomicLong(0);

        public Node(long timeStamp) {
            this.timeStamp = timeStamp;
        }

        private long saveThePadding() {
            // deceive jdk7 compiler
            return padding3 + padding4 + padding5 + padding6;
        }
    }

}
