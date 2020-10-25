package org.shoulder.core.uuid;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shoulder 基于 SnowFlake 算法改进
 * <p>
 * 在解决了标准的 snowflake 的不足：兼容时钟回拨处理、序列号满阻塞、bit位可配置（调整时间单元大小、序列大小、机器号范围）；
 * 测试结果：不会因为线程数增加带来的并发冲突/内存共享失效，产生性能下降
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
     * 最新的时间戳，位置，这两个要保证一起操作，这里使用 volatile 防止重排序、外加cas即可保证
     */

    private final AtomicLong latestTimeStamp = new AtomicLong(-1);

    //private volatile long latestTimeStamp;
    private volatile int latestIndex;

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
            buffer[i] = new Node(-1, 0);
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

        // 可以使用的位数 long 类型去掉符号位
        final int longBits = 64 - 1;
        if (timeStampBits + instanceIdBits + sequenceBits != longBits) {
            throw new IllegalArgumentException("timeStampBits + instanceIdBits + sequenceBits != 63 must = 63. " +
                "timeStampBits=" + timeStampBits + "instanceIdBits=" + instanceIdBits + "sequenceBits=" + sequenceBits);
        }
    }

    // ---------------------- 核心实现 -------------------------

    @Override
    public long nextId() {
        long timeStamp = currentTimeStamp();
        long lts;
        int lIndex;
        do {
            lts = latestTimeStamp.get();
            lIndex = latestIndex;
        } while (lts != latestTimeStamp.get());

        // 缓存中最旧的时间戳
        final long minTs = lts - bufferSize;
        if (timeStamp < minTs) {
            // 时钟回拨过多，一般不会触发
            onTimeBackTooMuch(timeStamp, latestTimeStamp.get(), minTs - timeStamp);
        }

        // 尝试拿 Node
        while (true) {
            // todo use mask
            final long timeStampFinal = timeStamp;
            int index = (lIndex + (int) (lts - timeStampFinal)) % bufferSize;
            index = index < 0 ? index + bufferSize : index;
            Node node = getNodeAt(index);
            if (node.timeStamp != timeStampFinal) {
                // 该时间戳内第一次访问，new 新的 CAS 替换
                Node newNode = new Node(timeStampFinal, 1, ((timeStampFinal - timeEpoch) << timestampLeftShift)
                    | (instanceId << instanceIdShift));
                if (casNodeAt(index, node, newNode)) {
                    // 当且仅当没人更新时更新
                    long old = latestTimeStamp.getAndUpdate(o -> Math.max(timeStampFinal, o));
                    // 判断是否更新过，替代 readWriteLock / DCL with sync
                    boolean hasUpdateTimeStamp = timeStampFinal > old;
                    if (hasUpdateTimeStamp) {
                        // update index
                        latestIndex = index;
                    }
                    return newNode.template;
                }

                // 失败则循环
            } else {
                // 该时间段内已经有其他线程已经设置过了
                long currentSequence = node.sequence.getAndIncrement() & sequenceMask;
                if (currentSequence != 0) {
                    return node.template | currentSequence;
                }
                // 超出单时间段内最大限制，说明该时间段内竞争非常激烈，使用下一时间段的数据
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

    /**
     * 批量生成 ID（无法充分利用CPU特性，因此单次获取较少，如100个以下时，推荐使用循环获取单个，否则性能会急剧下降，另外使用长度为2的整数次幂时，性能会有明显提升）
     * 测试发现，使用snowflakes默认格式，若总量需要1亿个
     * - nextId 方式需要1.8s
     * - nextIds(1) 方式需要 24s
     * - nextIds(32) 方式需要 18s
     * - nextIds(64) 方式需要 4.6s
     * - nextIds(100) 需要 1.6s
     * - nextIds(128) 需要 1.2s 【比较给力一个值】
     * - nextIds(1000)需要0.5s
     * - nextIds(4096)需要0.3s
     * 实际生产中，可以忽略
     *
     * @param num 批量生成个数。若实现原子性批量生成方法，注意控制单次生成个数过多（可能引起阻塞其他线程等）
     * @return guid
     */
    @Override
    public long[] nextIds(int num) {
        int maxSequence = 1 << instanceIdShift;

        if (num < 1 || num > maxSequence) {
            throw new IllegalArgumentException("num not allowed!");
        }
        long[] result = new long[num];
        long timeStamp = currentTimeStamp();
        long lts;
        int lIndex;
        do {
            lts = latestTimeStamp.get();
            lIndex = latestIndex;
        } while (lts != latestTimeStamp.get());

        // 缓存中最旧的时间戳
        final long minTs = lts - bufferSize;
        if (timeStamp < minTs) {
            // 时钟回拨过多（极其少见）
            onTimeBackTooMuch(timeStamp, latestTimeStamp.get(), minTs - timeStamp);
        }

        AtomicInteger gotCounter = new AtomicInteger();
        // 尝试拿 Node

        while (true) {
            final int got = gotCounter.get();
            final long timeStampFinal = timeStamp;
            int need = num - got;
            int index = ((int) (lIndex + lts - timeStampFinal)) % bufferSize;
            index = index < 0 ? index + bufferSize : index;
            Node node = getNodeAt(index);
            if (node.timeStamp != timeStampFinal) {
                // 该时间戳内第一次访问该 node，new 新的 CAS 替换，一次性获取剩余所需序列
                Node newNode = new Node(timeStampFinal, num - got);
                if (casNodeAt(index, node, newNode)) {
                    // 当且仅当没人更新时更新最新标记
                    long old = latestTimeStamp.getAndUpdate(o -> Math.max(timeStampFinal, o));
                    // 判断是否更新过，替代 readWriteLock / DCL with sync
                    boolean hasUpdateTimeStamp = timeStampFinal > old;
                    if (hasUpdateTimeStamp) {
                        // update index
                        latestIndex = index;
                    }
                    // 生成剩余所需所有 id
                    for (int i = 0; i < num - got; i++) {
                        result[got + i] = ((timeStampFinal - timeEpoch) << timestampLeftShift)
                            | (instanceId << instanceIdShift) | i;
                    }
                    // got = num;
                    return result;
                }
                // 失败则循环
            } else {
                // node.timeStamp == timeStamp，该时间段内已经有其他线程已经设置过了
                AtomicLong currentOldValue = new AtomicLong();
                long lastGotSequence = node.sequence.updateAndGet(oldValue -> {
                    long newValue;
                    currentOldValue.set(oldValue);
                    return (newValue = (int) oldValue + need) < maxSequence ? newValue : maxSequence;
                });
                final long oldValue = currentOldValue.get();
                int currentGotNum = (int) (lastGotSequence - oldValue);
                if (currentGotNum != 0) {
                    gotCounter.addAndGet(currentGotNum);
                    // 成功，生成剩余所需所有 id
                    for (int i = 0; i < currentGotNum; i++) {
                        result[got + i] = ((timeStamp - timeEpoch) << timestampLeftShift)
                            | (instanceId << instanceIdShift) | (oldValue + i);
                    }
                    // got = num;
                    if (got + currentGotNum == num) {
                        return result;
                    }
                }
                // 超出单时间段内最大限制，说明该时间段内竞争非常激烈，使用下一时间段的数据
                timeStamp = timeStamp + 1;
            }
        }
    }


    /**
     * 返回当前时间戳
     * 默认为当前系统毫秒数，共 41 位。如果访问量很大 / 时间位数较少 可以对其进行 lazy 化缓存提高运行效率
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

    public static class Node {

        final long timeStamp;

        /**
         * ((timeStamp - timeEpoch) << timestampLeftShift) | (instanceId << instanceIdShift)
         * 顺便拿出8 bytes利用一下缓存行（L1 cache 快于本次计算）
         */
        final long template;

        // 干掉内存伪共享，效果不明显
        //public volatile long padding1, padding2, padding3, padding4, padding5, padding6, padding7;
        // --------------- cacheLine ---------------

        // 下一个可用的 sequence (并发竞争点)
        final AtomicLong sequence;

        public Node(long timeStamp, long sequence) {
            this(timeStamp, sequence, 0);
        }

        public Node(long timeStamp, long sequence, long template) {
            this.timeStamp = timeStamp;
            this.sequence = new AtomicLong(sequence);
            this.template = template;
        }

    }

}
