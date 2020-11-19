package org.shoulder.core.guid;

import org.shoulder.core.util.PaddedAtomicLong;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shoulder 基于 SnowFlake 算法改进
 * <p>
 * 在解决了标准的 snowflake 的不足：兼容时钟回拨处理、序列号满阻塞、bit位可配置（调整时间单元大小、序列大小、机器号范围）；
 * 测试结果：不会因为线程数增加带来的并发冲突/内存共享失效，产生性能下降
 * <p>
 * 每个 id 有64bit，由时间段、实例标识、序列号 组成，各个部分长度可以自由配置，这里以 twitter 提出的雪花算法为例介绍 id 组成
 * <pre>{@code
 * #=======#------+----------------------+------------+--------------+
 * # total # sign |      time segment    | instanceId |   sequence   |
 * #=======#------+----------------------+------------+--------------+
 * # 64bit # 1bit |        41bits        |   10bits   |    12bits    |
 * #=======#------+----------------------+------------+--------------+
 * }</pre>
 * <p>
 * 高性能：1. 序列号CAS自增；2. 时钟缓存；3. 线程id hash 映射取缓存减少竞争；4. 使用 CAS 替代加锁 5.合理利用 CPU cache；5. 位移替代加、减、取余
 * Js 兼容：1. 字符串传输；2. 禁止将业务 id 传输给前端；3. 减少精度
 * 可扩展：自定义各段数据长度、整体长度、时间精度、时钟回拨逻辑、串解析策略
 * - 时钟回拨处理策略举例：1. 缓存生成历史【默认】；2. 机器标识减半； 3. 阻塞； 4. 记录日志、抛异常、告警...
 * <p>
 * 【该类为生成器框架，可以自行基础该类实现自己的生成器，在扩展同时，shoulder帮你实现了高并发的设计】
 *
 * @author lym
 * @see SnowFlakeGenerator 提供了雪花算法格式的实现
 */
public class ShoulderGuidGenerator implements LongGuidGenerator {

    // ---------------------- Fields（7个不变的） -------------------------

    /**
     * 实例标识 左移位数，空出自增序列所占位
     */
    private final long instanceIdShift;

    /**
     * 时间截 左移位数，空出自增序列所占位
     */
    private final long timestampLeftShift;

    /**
     * 生成序列的掩码，保证序列安全生成
     * 可通过 ~(-1L << instanceIdShift) 计算得到，但这里充分利用一下为解决伪共享而填充的 cacheLine（后续若新增变量，可以将该属性去除）
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
     * 每个 Node 存储了一个时间周期的数据
     */
    private final Node[] buffer;

    /**
     * buffer handle
     */
    private final VarHandle nodeHandle = MethodHandles.arrayElementVarHandle(Node[].class);

    // ---------------------- cacheLine split -------------------------

    /**
     * 最新的时间戳
     * 该变量需要CAS，还要保证附近代码防重排，因此选择 AtomicLong；最后再额外填充 6 个，补齐缓存行
     */
    private final AtomicLong latestTimeStamp = new PaddedAtomicLong(-1);

    // ---------------------- 构造器 -------------------------

    /**
     * 初始化 id 自增器格式
     *
     * @param timeStampBits  时间戳所占位数 > 0
     * @param timeEpoch      元时间戳（位数必须与 timeStampBits 一致）
     * @param instanceIdBits 应用实例标识（机器标识）位数 >= 0，必填，因为后续可能不一定是64位，不使用 64 减其他位计算
     * @param instanceId     实例标识
     * @param sequenceBits   单位时间（时间戳相关）内自增序列bit位数 > 0
     * @param bufferSize     缓冲区大小，用于抵抗时钟回拨，抵抗大小为[时间单位]*[bufferSize]；范围：[1, 1 << 30]，1 时不能抵抗时钟回拨，但性能最高
     *                       该值越小，则对时钟回拨处理能力越弱，在满负载生成时，性能越小
     *                       该值越大，则对时钟回拨处理能力越强，越大在满负载生成时对性能有一定影响
     *                       注意：参数设置合理时，几乎任何业务场景都不会触发满负载，无需关注性能问题
     */
    public ShoulderGuidGenerator(long timeStampBits, long timeEpoch,
                                 long instanceIdBits, long instanceId,
                                 long sequenceBits, int bufferSize) {
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

        // 可以使用的位数 long 类型去掉符号位 todo 【扩展性】 是否去掉该校验，以支持用户生成非 64bit的 long，如 js 只支持 53bit
        final int longBits = 64 - 1;
        if (timeStampBits + instanceIdBits + sequenceBits != longBits) {
            throw new IllegalArgumentException("timeStampBits + instanceIdBits + sequenceBits != 63 must = 63. " +
                "timeStampBits=" + timeStampBits + "instanceIdBits=" + instanceIdBits + "sequenceBits=" + sequenceBits);
        }

        // 初始化缓冲区
        this.buffer = new Node[bufferSizeFor(bufferSize)];
        Arrays.fill(buffer, new Node(-1, 0, -1));
    }

    /**
     * Returns a power of two table size for the given desired capacity.
     */
    private static final int bufferSizeFor(int s) {
        int maximumCapacity = 1 << 30;
        int n = -1 >>> Integer.numberOfLeadingZeros(s - 1);
        return (n < 0) ? 1 : (n >= maximumCapacity) ? maximumCapacity : n + 1;
    }

    // ---------------------- 核心实现 -------------------------

    @Override
    public long nextId() {
        int maxSequence = 1 << instanceIdShift;
        long timeStamp = currentTimeStamp() - timeEpoch;
        // 最新的时间戳
        long lts = latestTimeStamp.get();
        // 最旧的时间戳
        final long minTs = lts - buffer.length;
        if (timeStamp < minTs) {
            // 时钟回拨过多，提前透支时间过多（>bufferSize）均会触发
            onTimeBackTooMuch(timeStamp, latestTimeStamp.get(), minTs - timeStamp);
        }

        // 尝试拿 Node
        while (true) {
            final long timeStampFinal = timeStamp;
            int index = ((int) (timeStampFinal)) & (buffer.length - 1);
            Node node = getNodeAt(index);
            if (timeStampFinal > node.timeStamp) {
                // 处理预支时间超出当前时间过多? 默认不处理，除非使用者要求特殊限制，自行于 onTimeBackTooMuch 处理
                // 该时间戳内第一次访问，new 新的 CAS 替换
                Node newNode = new Node(timeStampFinal, 1, (timeStampFinal << timestampLeftShift)
                    | (instanceId << instanceIdShift));
                if (casNodeAt(index, node, newNode)) {
                    latestTimeStamp.incrementAndGet();
                    return newNode.template;
                }
                // 失败则循环
            }
            // 1. 上面竞争失败循环进入这里 2. 同一时间段（毫秒）内重复调用 3. 时间回拨 / 时间透支过多
            long currentSequence = node.sequence.getAndIncrement();
            if (currentSequence < maxSequence) {
                return node.template | (currentSequence & sequenceMask);
            }
            // 超出单时间段内最大限制，说明该时间段内竞争非常激烈，使用下一时间段的数据，重置序列，防止重试了 buffer次，回来
            timeStamp = timeStamp + 1;
        }
    }

    @Override
    public Map<String, String> decode(long snowflakeId) {
        Map<String, String> map = new HashMap<>(3);
        long relativeTimeStamp = snowflakeId >> timestampLeftShift;
        // 相对(元时间)时间戳
        map.put("relativeTimeStamp", String.valueOf(relativeTimeStamp));

        // 生成该id时的时间戳
        map.put("timestamp", String.valueOf(relativeTimeStamp + timeEpoch));

        // 自增序列
        long sequence = snowflakeId & sequenceMask;
        map.put("sequence", String.valueOf(sequence));

        // 实例标识
        long instanceId = snowflakeId >> instanceIdShift & ~(-1 << (timestampLeftShift - instanceIdShift));
        map.put("instanceId", String.valueOf(instanceId));
        return map;
    }

    /**
     * 批量生成 ID，若每次获取均为相同的2的整数次幂，可以看作原子性批量生成（生成的id是紧凑连续的）
     * num很小时（小于8）无法充分利用CPU特性。num 越大性能越高，使用 maxSequence 作为 num 时，性能有明显提升
     * <p>
     * 测试发现，使用snowflakes默认格式，若总量需要 1kw 个
     * - nextId 方式需要 135ms
     * - nextIds(1) 方式需要 225ms, 1 -> 8 所需时间减少
     * - nextIds(16-4095) 方式需要 65ms 左右
     * - nextIds(4096)需要 40 ms
     * 实际生产中，可以忽略
     *
     * @param num 批量生成个数，最大为 maxSequence 【扩展性】是否考虑支持更大的批量获取数？
     * @return guid
     */
    @Override
    public long[] nextIds(int num) {
        int maxSequence = 1 << instanceIdShift;

        if (num < 1 || num > maxSequence) {
            throw new IllegalArgumentException("num must less than maxSequence(" + maxSequence + ")!");
        }
        long[] result = new long[num];
        long timeStamp = currentTimeStamp() - timeEpoch;
        long lts = latestTimeStamp.get();

        // 缓存中最旧的时间戳
        final long minTs = lts - buffer.length;
        if (timeStamp < minTs) {
            // 时钟回拨过多（极其少见）
            onTimeBackTooMuch(timeStamp, latestTimeStamp.get(), minTs - timeStamp);
        }

        int gotCounter = 0;

        while (true) {
            // 本次循环前获取到 id 的个数
            final long timeStampFinal = timeStamp;
            int need = num - gotCounter;
            int index = ((int) (timeStampFinal)) & (buffer.length - 1);
            Node node = getNodeAt(index);
            if (timeStampFinal > node.timeStamp) {
                // 该时间戳内第一次访问该 node，new 新的 CAS 替换，一次性获取剩余所需序列
                Node newNode = new Node(timeStampFinal, need,
                    (timeStampFinal << timestampLeftShift) | (instanceId << instanceIdShift));
                if (casNodeAt(index, node, newNode)) {
                    // 当且仅当没人更新时更新最新标记
                    latestTimeStamp.incrementAndGet();
                    // 生成剩余所需所有 id
                    for (int i = gotCounter; i < gotCounter + need; i++) {
                        result[i] = newNode.template | i;
                    }
                    return result;
                }
            }
            for (long currentOldValue = node.sequence.get(); currentOldValue < maxSequence; currentOldValue = node.sequence.get()) {
                long canGet = maxSequence - currentOldValue;
                long tryGet = canGet > need ? need : canGet;
                if (node.sequence.compareAndSet(currentOldValue, currentOldValue + tryGet)) {
                    // 成功拿到，生成拿到的 id
                    for (int i = gotCounter; i < gotCounter + tryGet; i++) {
                        result[i] = node.template | (i + currentOldValue);
                    }
                    // got = num;
                    if (gotCounter + tryGet == num) {
                        return result;
                    }
                }
            }
            timeStamp = timeStamp + 1;
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
     * 时间回拨幅度过大，或时间透支过多
     * 默认不做任何处理，因为时间回滚后，将使用对应位置的timeStamp按正常流程尝试
     * 若此时同时透支过多，则继续透支，绝大部分情况不会有问题，性能也最高
     *
     * @param timeStamp       本次使用的时间戳
     * @param latestTimeStamp 本生成器记录的回拨前最晚时间
     * @param minDuration     最少等待时间
     */
    protected void onTimeBackTooMuch(long timeStamp, long latestTimeStamp, long minDuration) {
        // 该位置作为保留扩展点，使用者可以在这里可以做自己想做的事，如 自适应阻塞 / 更换 instanceId/ 抛异常处理 / 记录日志 / 指标监控 / 告警等
        /*throw new IllegalStateException("Clock moved backwards or overdraft too much. " +
            "It will auto cover in " + minDuration + "ms.");*/
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

        /**
         * 下一个可用的 sequence (并发竞争点)
         */
        final AtomicLong sequence;

        public Node(long timeStamp, long sequence, long template) {
            this.timeStamp = timeStamp;
            this.sequence = new AtomicLong(sequence);
            this.template = template;
        }

    }

}
