package org.shoulder.core.uuid;

/**
 * 类 SnowFlake 算法改进
 *
 * @author lym
 */
public class BaseShoulderLongGuidGenerator implements LongGuidGenerator {

    // ==============================Fields===========================================

    /**
     * 元时间截
     */
    private final long timeEpoch;

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
     * 当前服务实例标识
     */
    private long instanceId;

    /**
     * 上次生成ID的非自增部分
     */
    private long lastTemplate = -1L;

    //fixme 使用缓存时间轮
    private long sequence = -1L;

    private long lastTimestamp = -1L;

    /**
     * 初始化 id 自增器格式
     *
     * @param timeStampBits  时间戳所占位数
     * @param timeEpoch      元时间戳（位数必须与 timeStampBits 一致）
     * @param instanceIdBits 应用实例标识（机器标识）位数
     * @param instanceId     实例标识
     * @param sequenceBits   自增序列长度
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
        synchronized (this) {
            long timestamp = timeStamp();

            // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
            boolean timeBack = timestamp < lastTimestamp;
            if (timeBack) {
                // 抛出异常
                throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate autoconfigure for %d milliseconds", lastTimestamp - timestamp));
            }

            //如果是同一时间生成的，则进行毫秒内序列
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & sequenceMask;
                //毫秒内序列溢出
                if (sequence == 0) {
                    //阻塞到下一个毫秒,获得新的时间戳
                    //timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                //时间戳改变，毫秒内序列重置
                sequence = 0L;
            }

            //上次生成ID的时间截
            lastTimestamp = timestamp;

            //移位并通过或运算拼到一起组成64位的ID
            return ((timestamp - timeEpoch) << timestampLeftShift)
                | (instanceId << instanceIdShift)
                | sequence;
        }
    }

    @Override
    public Long[] nextIds(int num) {
        return new Long[0];
    }


    /**
     * 返回当前时间戳
     * 默认为当前毫秒数，共 41 位
     *
     * @return 当前时间戳
     */
    protected long timeStamp() {
        return System.currentTimeMillis();
    }

    /**
     * 返回下一个自增序列号
     *
     * @return 当前时间戳 fixme 没有实现
     */
    protected long sequence() {
        return 1L;
    }

    // 达到本秒内的上限

    // 时间回拨

}
