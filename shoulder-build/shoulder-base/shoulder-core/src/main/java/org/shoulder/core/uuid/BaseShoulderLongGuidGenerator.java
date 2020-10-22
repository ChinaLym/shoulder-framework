package org.shoulder.core.uuid;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 类 SnowFlake 算法改进
 *
 * @author lym
 */
public class BaseShoulderLongGuidGenerator implements LongGuidGenerator {

    // ==============================Fields===========================================

    /**
     * 时间戳 占用位数
     */
    private final long timeStampBits;

    /**
     * 自增序列号 占用位数
     */
    private final long instanceIdBits;

    /**
     * 自增序列号 占用位数
     */
    private final long sequenceBits;

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
     * 上次生成ID的非自增部分
     */
    private long lastTemplate = -1L;

    //fixme 使用缓存时间轮，减少 sequence 的竞争，又能兼容时间戳回滚，时间轮内部采用 AutomicLong
    private volatile long sequence = -1L;

    private volatile long lastTimestamp = -1L;

    /**
     * 初始化 id 自增器格式
     *
     * @param timeStampBits  时间戳所占位数
     * @param timeEpoch      元时间戳（位数必须与 timeStampBits 一致）
     * @param instanceIdBits 应用实例标识（机器标识）位数
     * @param instanceId     实例标识
     * @param sequenceBits   单位时间（时间戳相关）内自增序列bit位数
     */
    public BaseShoulderLongGuidGenerator(long timeStampBits, long timeEpoch,
                                         long instanceIdBits, long instanceId,
                                         long sequenceBits) {
        // guid 格式
        this.timeStampBits = timeStampBits;
        this.instanceIdBits = instanceIdBits;
        this.sequenceBits = sequenceBits;

        this.timeEpoch = timeEpoch;
        this.instanceId = instanceId;

        // 初始化与校验
        instanceIdShift = sequenceBits;
        timestampLeftShift = sequenceBits + instanceIdShift;
        sequenceMask = ~(-1L << sequenceBits);

        // 时间戳 所占位数必须大于零，且给定元时间戳也需要符合该限制
        int timeEpochBits = 64 - Long.numberOfLeadingZeros(timeEpoch);
        if (timeStampBits <= 0 || timeStampBits < timeEpochBits) {
            throw new IllegalArgumentException("timeStampBits or timeEpoch invalid. " +
                "timeStampBits=" + timeStampBits + ", timeEpoch=" + timeEpoch);
        }

        // 实例标识 所占位数必须大于零，且给定实例标识也需要符合该限制
        int selfInstanceIdBits = 64 - Long.numberOfLeadingZeros(timeEpoch);
        if (instanceIdBits < 0 || timeStampBits < selfInstanceIdBits) {
            throw new IllegalArgumentException("instanceIdBits or instanceId invalid. " +
                "instanceIdBits=" + instanceIdBits + ", instanceId=" + instanceId);
        }

        // 序列号所占位数必须大于 0
        if (sequenceBits <= 0) {
            throw new IllegalArgumentException("sequenceBits must > 0. sequenceBits=" + sequenceBits);
        }
    }


    @Override
    public Long nextId() {
        // 获取时间戳
        final long lts = this.lastTimestamp;
        long timestamp = timeStamp();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
        boolean timeBack = timestamp < lts;
        if (timeBack) {
            onTimeBack();
        }

        // 序列号
        final long seq = sequence;
        final long currentSequence = (sequence + 1) & sequenceMask;

        // 此时时间戳已经拿到，只需要看是否合法（获取到的序列不是0），如果不等于上次，CAS 修改最新时间戳
        boolean success = compareAndSetObject(lastTimestamp, lts, lastTimestamp);
        if (success) {
            // 特殊处理：若成功则直接使用自增序列为0的，因为大多时候是没有竞争的
            // todo 设置为0
            return ((timestamp - timeEpoch) << timestampLeftShift)
                | (instanceId << instanceIdShift)
                | sequence;
        } else {
            // todo 否则说明其他线程已经修改，
        }

        synchronized (this) {
            // 时间戳相同，CAS 增加序列号
            if (lts == timestamp) {
                sequence = (sequence + 1) & sequenceMask;
                //毫秒内序列溢出
                if (sequence == 0) {
                    //阻塞到下一个毫秒,获得新的时间戳
                    //timestamp = tilNextMillis(lastTimestamp);
                }
            } else {

                sequence = 0L;
            }
        }

        // 序列已经拿到，CAS 更新上次生成ID的时间截（当且仅当lts == lts）
        lastTimestamp = timestamp;

            //移位并通过或运算拼到一起组成64位的ID
            return ((timestamp - timeEpoch) << timestampLeftShift)
                | (instanceId << instanceIdShift)
                | sequence;
    }

    @Override
    public Long[] nextIds(int num) {
        return new Long[0];
    }


    /**
     * 返回当前时间戳 默认为当前毫秒数，共 41 位。如果访问量很大可以对其进行 lazyTime 缓存
     *
     * @return 当前时间戳
     */
    protected long timeStamp() {
        return System.currentTimeMillis();
    }

    /**
     * 返回下一个自增序列号
     *
     * @return 下一个自增序列号 fixme 没有实现
     */
    protected long sequence() {
        return 1L;
    }

    // 达到本秒内的上限

    // 时间回拨

    /**
     * 处理时间回拨
     *
     * @return
     */
    protected long onTimeBack() {
        // 默认回拨处理：抛出异常，也可考虑 await 阻塞
        throw new RuntimeException("Clock moved backwards. Refusing to generate");
    }


    public static class TimeStampSequenceRingBuffer {
        /**
         * 最小的时间戳
         */
        volatile long lowTimeStamp;

        volatile int lowIndex;
        /**
         * 最新的时间戳，位置，这两个要保证一起操作
         */
        volatile long latestTimeStamp;
        volatile int latestIndex;

        final int bufferSize = 1024;

        Node[] buffer = new Node[bufferSize];

        ReadWriteLock lock = new ReentrantReadWriteLock();

        /**
         * 根据时间戳获取
         *
         * @param timeStamp
         * @return
         */
        Node getNode(long timeStamp) {
            lock.readLock().lock();
            final long lts = latestTimeStamp;
            final long lIndex = latestIndex;
            lock.readLock().unlock();

            final long minTs = lts - bufferSize;
            if (timeStamp < minTs) {
                // 时钟回拨幅度过大，todo 阻塞 / 异常 (少见)
                throw new IllegalStateException("Clock moved backwards. Refusing to generate");
            } else {
                // 尝试拿，若拿不到，创建新的，set 进去
                int index = ((int) (lIndex + (lts - timeStamp) + bufferSize)) % bufferSize;
                Node node = buffer[index];
                if (node.timeStamp == timeStamp) {
                    // 使用该 node 自增序列号
                    return node;
                } else {
                    // CSA 设置时间戳，成功则获取0序列

                    // 失败，且时间戳相等则返回node，否则抛异常【时钟回拨临近极限，刚才判断是否回拨过久导致的时还未过期，现在过期了】
                }
            }
        }


        public synchronized void nextStep(int step) {
            lock.writeLock().lock();
            lowTimeStamp += step;
            lowIndex += step;
            lowIndex = lowIndex % bufferSize;
            lock.writeLock().unlock();
        }

        /**
         * 与当前跨度太大，缓存不存在，直接
         */
        private void reset() {

        }

    }


    public static class Node {

        long timeStamp;

        AtomicLong sequence = new AtomicLong(0);

        public Node(long timeStamp) {
            this.timeStamp = timeStamp;
        }

    }



}
