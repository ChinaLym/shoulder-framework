package org.shoulder.core.guid.impl;

import cn.hutool.core.lang.Snowflake;

/**
 * Twitter_Snowflake 格式
 * 41 位时间戳，5位数据中心标识 5位机器标识 12 位自增序列号
 * SnowFlake的结构如下(每部分用-分开)
 * <pre>{@code
 *   0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000 <br>
 * 符号位 \------------------ 时间戳 ---------------/   \---机器标识--/  \---序列号---/ <br>
 * }</pre>
 * <p>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0<br>
 * 41位时间截(毫秒级)。注：该值为（当前时间截 - 元时间截)，而非当前时间截，可使用69年（2^41次方除每年的毫秒数）
 * 元时间截一般为投入生产运行时的时间，以保证更长久的使用 见 {@link #timeEpoch}<br>
 * 10位的数据机器位，即可以部署在1024个服务节点上，可分为5位dataCenterId和5位workerId<br>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
 * 以上共64位，即一个Long型。<br>
 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 *
 * @author lym
 * @see Snowflake hutool 中也有提供雪花算法的实现，但性能远不及 shoulder 的实现
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
