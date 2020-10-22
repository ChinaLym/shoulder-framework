package org.shoulder.core.uuid;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
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
     * 最新的时间戳，位置，这两个要保证一起操作
     */
    private volatile long latestTimeStamp;
    private volatile int latestIndex;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

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
        long timestamp = currentTimeStamp();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
        boolean timeBack = timestamp < lts;
        if (timeBack) {
            return onTimeBack();
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
                // 毫秒内序列溢出
                if (sequence == 0) {
                    // 阻塞到下一个毫秒,获得新的时间戳（如果跑到这里，说明你的业务量的价值富可敌国了）
                    timestamp = tilNextStamp(lastTimestamp);
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
     * 返回当前时间戳
     * 默认为当前毫秒数，共 41 位。如果访问量很大 / 时间位数较少 可以对其进行 lazy 化缓存提高运行效率
     *
     * @return 当前时间戳
     */
    protected long currentTimeStamp() {
        return System.currentTimeMillis();
    }

    /**
     * 返回下一个自增序列号
     *
     * @return 下一个自增序列号 fixme 没有实现
     */
    protected synchronized long nextSequence(long timeStamp) {
        return sequence = (sequence + 1) & sequenceMask;
    }

    /**
     * 达到本秒内的上限，【阻塞】至下一个时间戳
     * 默认使用 while 循环实现，原因：自增序列满了，说明这一毫秒内已经进行了非常多次的调用，故需要阻塞时间通常远小于 1ms，直接使用循环，注意CPU毛刺
     *
     * @param currentTimestamp 当前时间戳
     * @return currentTimestamp + 1
     */
    protected long tilNextStamp(long currentTimestamp) {
        long mill = currentTimeStamp();
        while (mill <= currentTimestamp) {
            mill = currentTimeStamp();
        }
        return mill;
    }

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
         * 最新的时间戳，位置，这两个要保证一起操作
         */
        volatile long latestTimeStamp;
        volatile int latestIndex;
        ReadWriteLock lock = new ReentrantReadWriteLock();

        final int bufferSize;

        final Node[] buffer;

        VarHandle nodeHandle = MethodHandles.arrayElementVarHandle(Node[].class);

        TimeStampSequenceRingBuffer() {
            this(1024);
        }

        TimeStampSequenceRingBuffer(int bufferSize) {
            this.bufferSize = bufferSize;
            // 初始化缓冲区
            buffer = new Node[bufferSize];
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = new Node(-1);
            }
        }

        /**
         * 根据时间戳获取
         *
         * @param timeStamp 时间戳
         * @return 序列
         */
        long next(long timeStamp) {
            lock.readLock().lock();
            final long lts = latestTimeStamp;
            final long lIndex = latestIndex;
            lock.readLock().unlock();

            // 缓存中最旧的时间戳
            final long minTs = lts - bufferSize;
            if (timeStamp < minTs) {
                // 时钟回拨幅度过大（少见）
                throw new IllegalStateException("Clock moved backwards. Refusing to generate");
            }

            // 尝试拿 Node
            while (true) {
                int index = ((int) (lIndex + (lts - timeStamp) + bufferSize)) % bufferSize;
                Node node = getNodeAt(index);
                if (node.timeStamp != timeStamp) {
                    // 该时间戳内第一次访问，new 新的 CAS 替换
                    Node newNode = new Node(timeStamp);
                    if (casNodeAt(index, node, newNode)) {
                        // 成功时序列为 0
                        return ((timestamp - timeEpoch) << timestampLeftShift)
                            | (instanceId << instanceIdShift);
                    }
                    // 失败则循环
                    continue;
                } else {
                    // timeStamp == timeStamp，该毫秒内已经有其他线程已经设置过了
                    long currentSequence = node.sequence.incrementAndGet();
                    if (currentSequence != 0) {
                        return ((timestamp - timeEpoch) << timestampLeftShift)
                            | (instanceId << instanceIdShift) | currentSequence;
                    }
                    // 超出单毫秒内最大限制，说明该毫秒内竞争非常激烈，使用下一毫秒的数据
                    timeStamp = timeStamp + 1;
                }
            }

        }

        private Node getNodeAt(int index) {
            return (Node) nodeHandle.getAcquire(buffer, index);
        }

        private boolean casNodeAt(int index, Node old, Node newValue) {
            return nodeHandle.compareAndSet(buffer, index, old, newValue);
        }

        public synchronized void nextStep(int step) {
            lock.writeLock().lock();
            latestTimeStamp += step;
            latestIndex += step;
            latestIndex = latestIndex % bufferSize;
            lock.writeLock().unlock();
        }

    }


    /**
     * 缓存区，节点，保存时间/自增序列
     * 更新方式：读写锁 （固定个数锁变量，需处理极小概率事件 ‘时钟回拨临近缓冲上限’）
     * cas Node 方式（无锁，new 小对象）【默认】
     */
    public static class Node {

        final long timeStamp;

        final AtomicLong sequence = new AtomicLong(0);

        //final long leadingTemplate;

        public Node(long timeStamp) {
            this.timeStamp = timeStamp;
        }

        /*final ReadWriteLock lock = new ReentrantReadWriteLock();

        public void update(long timeStamp, long newSequence) {
            boolean holdLock = lock.writeLock().tryLock();
            if(holdLock) {
                this.timeStamp = timeStamp;
                sequence.set(newSequence);
                lock.writeLock().unlock();
            }
        }

        public long nextSequence(){
            lock.readLock().lock();
            long result = sequence.getAndIncrement();
            lock.readLock().unlock();
            return result;
        }*/
    }

}
