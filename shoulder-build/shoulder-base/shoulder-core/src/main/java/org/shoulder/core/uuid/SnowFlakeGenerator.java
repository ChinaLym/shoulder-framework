package org.shoulder.core.uuid;

/**
 * SnowFlake 格式
 * 41 位时间戳，5位数据中心标识 5位机器标识 12 位自增序列号
 *
 * @author lym
 */
public class SnowFlakeGenerator extends ShoulderGuidGenerator {

    /**
     * 默认元时间截 (2020-8-1)
     */
    public static final long DEFAULT_TIME_EPOCH = 1596211200000L;

    /**
     * 初始化 id 自增器格式
     *
     * @param dataCenterId 数据中心 id
     * @param machineId    机器号
     */
    public SnowFlakeGenerator(long dataCenterId, long machineId) {
        this(DEFAULT_TIME_EPOCH, dataCenterId, machineId);
    }

    /**
     * 初始化 id 自增器格式
     *
     * @param timeEpoch    元时间戳（位数必须与 timeStampBits 一致）
     * @param dataCenterId 数据中心 id
     * @param machineId    机器号
     */
    public SnowFlakeGenerator(long timeEpoch, long dataCenterId, long machineId) {
        // snowflake 算法中包含 41 位时间戳，5位数据中心标识 5位机器标识 12 位自增序列号，默认不对时间回退做处理
        super(41, timeEpoch,
            5 + 5, (dataCenterId << 5) | (machineId),
            12, 1);
    }

}
