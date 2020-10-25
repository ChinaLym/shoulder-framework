package org.shoulder.core.uuid;

import org.junit.Test;
import org.shoulder.core.util.JsonUtils;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * shoulder 开发的无锁化 Guid 生成器测试
 *
 * @author lym
 */
public class ShoulderGuidTest {

    /**
     * 生成 1亿次
     */
    private static final int GENERATE_NUM = 100_000_000;

    /**
     * 测试单次获取
     */
    @Test
    public void testSingle() {
        LongGuidGenerator generator = new SnowFlakeGenerator(1, 1);
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM; i++) {
            //System.out.println(generator.nextId());
            generator.nextId();
        }
        System.out.println("cost " + (System.currentTimeMillis() - start));
    }

    @Test
    public void testSingleNoRepeat() {
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 10, 0, 12, 1);
        BitSet bitSet = new BitSet(GENERATE_NUM);
        for (int i = 0; i < GENERATE_NUM; i++) {
            long id = generator.nextId();
            // must pressed！ or else will cause OOM crash! (pressed for stander snowflake: 10 bit time 12 bit sequence)
            int pressedId = press(id);
            bitSet.set(pressedId);
        }
        System.out.println(bitSet.cardinality());
        assert GENERATE_NUM == bitSet.cardinality();
    }

    /**
     * 压缩 long 为 int 方便统计是否重复
     * 要求：starter snowflakes、元时间为最近时间，否则可能压缩失败
     */
    private static int press(long id) {
        final long sequenceMask = ~(-1L << 12);
        return (int) ((id >> 22 << 12) | (id & sequenceMask));
    }


    /**
     * 测试批量获取，
     */
    @Test
    public void testMulti() {
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 10, 0, 12, 1);
        BitSet bitSet = new BitSet(GENERATE_NUM);
        long sequenceMask = ~(-1L << 12);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < GENERATE_NUM; ) {
            int getNum = 2048;
            long[] ids = generator.nextIds(getNum);
            for (int j = 0; j < ids.length; j++) {
                long id = ids[j];
                int pressedId = (int) ((id >> 22 << 12) | (id & sequenceMask));
                if (bitSet.get(pressedId)) {
                    System.out.println(generator.decode(id));
                    System.out.println(bitSet.cardinality());
                    throw new IllegalStateException("repeat!");
                }
                bitSet.set(pressedId);
            }
            i += getNum;
        }
        System.out.println(bitSet.cardinality());
        assert GENERATE_NUM == bitSet.cardinality();
    }

    /**
     * 测试批量获取，
     */
    @Test
    public void testMultiNoRepeat() {
        LongGuidGenerator generator = new SnowFlakeGenerator(1, 1);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM; ) {
            int getNum = 2048;
            generator.nextIds(getNum);
            i += getNum;
        }
        System.out.println("cost " + (System.currentTimeMillis() - start));
    }

    /**
     * 测试 twitter 的翻译实现，发现 shoulder 的生成速度是 twitterSnowFlakeIdGenerator.java 的近万倍！！
     * 注意，该算法固定单个生成器，1s最多生产4096个！性能可估，故不要测试十万级别以上的数据！
     */
    @Test
    public void testTwitter() {
        SnowFlakeIdGenerator snowFlakeIdGenerator = new SnowFlakeIdGenerator(1, 1);
        long start = System.currentTimeMillis();
        // 标准雪花算法不要使用 GENERATE_NUM，否则慢的离谱，这里只生成10w
        for (int i = 0; i < 100_000; i++) {
            //System.out.println(generator.nextId());
            snowFlakeIdGenerator.nextId();
        }
        System.out.println("cost " + (System.currentTimeMillis() - start));

    }

    /**
     * 测试高并发表现
     */
    @Test
    public void testHighPress() {
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 10, 0, 12, 1);
        final int totalThreadNum = 10;

        long start = System.currentTimeMillis();
        for (int threadNum = 0; threadNum < totalThreadNum; threadNum++) {
            new Thread(Thread.currentThread().getThreadGroup(),
                () -> {
                    for (int i = 0; i < GENERATE_NUM / totalThreadNum; ) {
                        generator.nextId();
                        i++;
                        //int once = 2048; generator.nextIds(once);i += once;
                    }
                },
                "worker-" + threadNum).run();
        }
        System.out.println("cost " + (System.currentTimeMillis() - start));
    }

    /**
     * 测试多线程并发获取，也不会有重复的
     */
    @Test
    public void testMultiThreadsHighPressNoRepeat() {
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 10, 0, 12, 1);
        final int totalThreadNum = 10;
        BitSet bitSet = new BitSet(GENERATE_NUM);
        long start = System.currentTimeMillis();
        for (int threadNum = 0; threadNum < totalThreadNum; threadNum++) {
            new Thread(Thread.currentThread().getThreadGroup(),
                () -> {
                    for (int i = 0; i < GENERATE_NUM / totalThreadNum; ) {
                        long id = generator.nextId();
                        i++;
                        bitSet.set(press(id));
                        //int once = 2048; generator.nextIds(once);i += once;
                    }
                },
                "worker-" + threadNum).run();
        }
        System.out.println("cost " + (System.currentTimeMillis() - start));
        System.out.println(bitSet.cardinality());
        assert GENERATE_NUM == bitSet.cardinality();
    }


    /**
     * 测试自定义扩展
     * （举例：针对单机场景做性能优化）
     * 实际没必要对它做进一步性能优化：生成 100w 数据时，仅快 5ms 左右。如果你的业务量能到 100w/s 做梦都要笑醒了，况且也不会差这几毫秒
     */
    @Test
    public void testExtension_41_0_22() {
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 0, 0, 22, 1);
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM; i++) {
            //System.out.println(generator.nextId());
            generator.nextId();
        }
        System.out.println("cost " + (System.currentTimeMillis() - start));
    }


    /**
     * 测试解码
     */
    @Test
    public void testDecode() {
        long timeEpoch = System.currentTimeMillis();
        long instanceIdBits = 10;
        long instanceId = ThreadLocalRandom.current().nextInt(1 << instanceIdBits);
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, timeEpoch, instanceIdBits, instanceId, 12, 1);

        long sequence = 0;
        long id = generator.nextId();
        long currentTimestamp = System.currentTimeMillis();
        System.out.println("current=" + currentTimestamp);
        System.out.println("instanceId=" + instanceId);
        System.out.println("genId=" + id);
        Map<String, String> decodeResult = generator.decode(id);
        System.out.println(JsonUtils.toJson(decodeResult));


        assert decodeResult.get("timestamp").equals(String.valueOf(currentTimestamp)) || decodeResult.get("timestamp").equals(String.valueOf(currentTimestamp) + 1);
        assert decodeResult.get("sequence").equals(String.valueOf(sequence));
        assert decodeResult.get("instanceId").equals(String.valueOf(instanceId));
    }


}
