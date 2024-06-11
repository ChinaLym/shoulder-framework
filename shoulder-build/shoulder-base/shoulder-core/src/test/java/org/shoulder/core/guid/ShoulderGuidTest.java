package org.shoulder.core.guid;

import org.junit.jupiter.api.Test;
import org.shoulder.core.guid.impl.ShoulderGuidGenerator;
import org.shoulder.core.guid.impl.SnowFlakeGenerator;
import org.shoulder.core.util.JsonUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.BitSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * shoulder 开发的无锁化 Guid 生成器测试
 * <p>
 * 注意：由于 guid 在使用中基本不会触发性能瓶颈，这里的性能测试仅为无预热粗略测试，精确测试请使用 JMH。
 *
 * @author lym
 */
public class ShoulderGuidTest {

    /**
     * 生成次数（默认 100w次，低性能机器适当调低，否则将过于卡顿或导致测试结果不准确）
     */
    private static final int GENERATE_NUM = 10_000_00;

    /**
     * 测试多线程生成时使用的核数
     */
    private static final int THREADS;

    static {
        // cpu 可用核数 - 1，防止低性能机器上过于卡顿
        int coreNum = 9;//Runtime.getRuntime().availableProcessors();
        THREADS = coreNum > 1 ? coreNum - 1 : 1;
        System.out.println("GENERATE_NUM = " + GENERATE_NUM);
        System.out.println("THRADS = " + THREADS);
        System.out.println("---------------------------------------");
    }

    // -------------------------------- 先感受以下 JDK UUID 性能 -------------------------------------

    /**
     * 【性能-JDK】单线程，每次获取1个，获取 GENERATE_NUM 次
     */
    //@Test
    public void timer_jdk_uuid() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM; i++) {
            UUID.randomUUID().toString().replace("-", "");
        }
        long end = System.currentTimeMillis() - start;
        System.out.println("TIME-jdk-uuid TEST: " + GENERATE_NUM + " generated by 1 threads cost " + end + " ms");
    }

    /**
     * 【性能】测试多线程超高并发获取性能，注意，高并发时由于大量CAS，将导致CPU飙升
     */
    //@Test
    public void timer_jdk_uuid_threads() throws InterruptedException {
        final int totalThreadNum = THREADS;
        CountDownLatch startLatch = new CountDownLatch(totalThreadNum);
        CountDownLatch finishLatch = new CountDownLatch(totalThreadNum);
        for (int threadNum = 0; threadNum < totalThreadNum; threadNum++) {
            Thread t = new Thread(Thread.currentThread().getThreadGroup(),
                () -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < GENERATE_NUM / totalThreadNum; i++) {
                            UUID.randomUUID().toString().replace("-", "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        finishLatch.countDown();
                    }
                }, "worker-" + threadNum);
            t.start();
            startLatch.countDown();
        }
        startLatch.await();
        long start = System.currentTimeMillis();
        finishLatch.await();
        System.out.println("TIME-jdk_uuid-threads TEST: " + GENERATE_NUM + " generated by " + totalThreadNum + " threads cost " +
            (System.currentTimeMillis() - start) + " ms");
    }

    // ------------------------------------- 正片开始 -------------------------------------

    /**
     * 【性能】单线程，每次获取1个，获取 GENERATE_NUM 次
     */
    @Test
    public void timer_nextId() {
        LongGuidGenerator generator = new SnowFlakeGenerator(1, 1);
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM; i++) {
            generator.nextId();
        }
        long end = System.currentTimeMillis() - start;
        System.out.println("TIME-nextId TEST: " + GENERATE_NUM + " generated by 1 threads cost " + end + " ms");
    }


    /**
     * 【性能】单线程，每次获取 getNum 个，总共获取 GENERATE_NUM 个
     */
    @Test
    public void timer_nextIds() {
        LongGuidGenerator generator = new SnowFlakeGenerator(1, 1);
        int onceFetch = 16;
        // 随机获取测试，该结果无法获取准确结果，因为生成随机数相对于 shoulder 的高性能id生成器来说，也是较挺耗时的，即使是 ThreadLocalRandom
        // ThreadLocalRandom random = ThreadLocalRandom.current();
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM / onceFetch; i++) {
            generator.nextIds(onceFetch);
        }
        long end = System.currentTimeMillis() - start;
        System.out.println("TIME-nextIds TEST: " + GENERATE_NUM + " generated by 1 threads cost " + end + " ms");
    }

    /**
     * 压缩 long 为 int 方便精确统计【亿级id是否重复】，直接使用 HashSet 等可能导致 OOM
     * 要求：starter snowflakes、元时间为最近时间，否则可能压缩失败
     */
    private static int press(long id) {
        // 符号位 ~(-1L << 32)
        // final long sequenceMask = ~(-1L << 12);
        return (int) (((id & ~(-1L << 32)) >> 22 << 12) | (id & ~(-1L << 12)));
    }

    /**
     * 【无重复】单线程，每次获取1个，获取 GENERATE_NUM 次，测试是否产生重复 id
     */
    @Test
    public void noRepeat_nextId() {
        BitSet bitSet = new BitSet(GENERATE_NUM);
        long lastId = -1; // 用于测试生成的id为递增
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 10, 0, 12, 1);
        for (int i = 0; i < GENERATE_NUM; i++) {
            long id = generator.nextId();
            // must pressed! or else will cause OOM crash! (pressed for stander snowflake: 10 bit time 12 bit sequence)
            int pressedId = press(id);
            bitSet.set(pressedId);
            assert id > lastId; // 断言递增
            lastId = id;
        }
        System.out.println(bitSet.cardinality());
        assert GENERATE_NUM == bitSet.cardinality();
    }


    /**
     * 【无重复、单调递增】单线程，每次获取 getNum 个，总共获取 GENERATE_NUM 个，测试是否产生重复 id
     */
    @Test
    public void noRepeatAndIncreasing_nextIds() {
        BitSet bitSet = new BitSet(GENERATE_NUM); // 亿级数据精准较重使用 BitSet，HashMap 会触发 OOM
        long lastId = -1; // 用于测试生成的id为递增
        long sequenceMask = ~(-1L << 12);
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 10, 0, 12, 1);
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM; ) {
            int addPerCircle = 4096;
            int getNum = i + addPerCircle > GENERATE_NUM ? GENERATE_NUM - i : addPerCircle;
            long[] ids = generator.nextIds(getNum);
            for (long id : ids) {
                int pressedId = (int) ((id >> 22 << 12) | (id & sequenceMask));
                bitSet.set(pressedId);
                assert id > lastId; // 断言单调递增
                lastId = id;
            }
            i += getNum;
        }
        long end = System.currentTimeMillis() - start;
        System.out.println("noRepeatSet.num: " + bitSet.cardinality());
        System.out.println("[NO-REPEAT & INCREASING]-nextIds TEST: " + GENERATE_NUM + " generated by 1 threads cost " + end + " ms");
        assert GENERATE_NUM == bitSet.cardinality(); // 断言无重复
    }


    /**
     * 【性能】测试多线程超高并发获取性能，说明：由于使用了不少CAS，高并发时会存在 CPU上升
     */
    @Test
    public void timer_nextId_threads() throws InterruptedException {
        final int totalThreadNum = THREADS;
        CountDownLatch startLatch = new CountDownLatch(totalThreadNum);
        CountDownLatch finishLatch = new CountDownLatch(totalThreadNum);
        final boolean fetchOne = false;
        int onceFetch = 4096;
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 10, 0, 12, 1);
        for (int threadNum = 0; threadNum < totalThreadNum; threadNum++) {
            Thread t = new Thread(Thread.currentThread().getThreadGroup(),
                () -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < GENERATE_NUM / totalThreadNum; ) {
                            if (fetchOne) {
                                generator.nextId();
                                i++;
                            } else {
                                generator.nextIds(onceFetch);
                                i += onceFetch;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        finishLatch.countDown();
                    }
                }, "worker-" + threadNum);
            t.start();
            startLatch.countDown();
        }
        startLatch.await();
        long start = System.currentTimeMillis();
        finishLatch.await();
        System.out.println("TIME-nextId-threads TEST: " + GENERATE_NUM + " generated by " + totalThreadNum + " threads cost " +
            (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * 【无重复】测试多线程超高并发获取，也不会有重复的
     */
    @Test
    public void noRepeat_threads() throws InterruptedException {
        final int totalThreadNum = THREADS;
        BitSet bitSet = new BitSet(GENERATE_NUM);
        final boolean fetchOne = true;
        int onceFetch = 2048;
        long sequenceMask = ~(-1L << 12);
        CountDownLatch startLatch = new CountDownLatch(totalThreadNum);
        CountDownLatch finishLatch = new CountDownLatch(totalThreadNum);
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 10, 0, 12, 1);
        for (int threadNum = 0; threadNum < totalThreadNum; threadNum++) {
            Thread t = new Thread(Thread.currentThread().getThreadGroup(),
                () -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < GENERATE_NUM / totalThreadNum; ) {
                            if (fetchOne) {
                                long id = generator.nextId();
                                int pressedId = press(id);
                            /*if (bitSet.get(pressedId)) {
                                System.out.println("i=" + i + "    decode: " + generator.decode(id));
                                System.out.println(bitSet.cardinality());
                                throw new IllegalStateException("repeat!");
                            }*/
                                synchronized (bitSet) {
                                    bitSet.set(pressedId);
                                }
                                i++;
                            } else {
                                long[] ids = generator.nextIds(onceFetch);
                                for (long id : ids) {
                                    int pressedId = (int) ((id >> 22 << 12) | (id & sequenceMask));
                                /*if (bitSet.get(pressedId)) {
                                    System.out.println("i=" + i + "    decode: " + generator.decode(id));
                                    System.out.println(bitSet.cardinality());
                                    throw new IllegalStateException("repeat!");
                                }*/
                                    synchronized (bitSet) {
                                        bitSet.set(pressedId);
                                    }
                                }
                                i += onceFetch;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        finishLatch.countDown();
                    }
                },
                "worker-" + threadNum);
            t.start();
            startLatch.countDown();
        }
        startLatch.await();
        long start = System.currentTimeMillis();
        finishLatch.await();
        long end = System.currentTimeMillis() - start;
        int realGen = GENERATE_NUM / totalThreadNum * totalThreadNum;
        System.out.println("NO-REPEAT-nextId-threads TEST: " + realGen + " generated by " + totalThreadNum + " threads cost " + end +
            " ms");
        System.out.println("generated: " + bitSet.cardinality());
        assert realGen == bitSet.cardinality();
    }


    /**
     * 【扩展性】测试自定义id格式
     * （举例：针对单机场景做性能优化）
     * 实际没必要对它做进一步性能优化：生成 100w 数据时，仅快 5ms 左右。如果您的业务量能到 100w/s 做梦都要笑醒了，况且也不会差这几毫秒
     */
    @Test
    public void extension_41_0_22() {
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
     * 【功能】测试解码
     */
    @Test
    public void decode() {
        long timeEpoch = System.currentTimeMillis();
        int instanceIdBits = 10;
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


    // ============================== 其他开源实现简单测试对比 =====================

    /**
     * 【其他开源实现】测试 twitter 的翻译实现，发现 shoulder 的生成速度是 twitterSnowFlakeIdGenerator.java 的近万倍！！
     * 注意，该算法固定单个生成器，1s最多生产4096个！性能可估，故不要测试十万级别以上的数据！
     * 单测时跳过，否则太慢了
     */
    //@Test
    public void third_twitter_snowflake() {
        SnowFlakeIdGenerator snowFlakeIdGenerator = new SnowFlakeIdGenerator(1, 1);
        Instant start = Instant.now();
        // 标准雪花算法不要使用 GENERATE_NUM，否则慢的离谱，这里只生成10w
        for (int i = 0; i < 100_000; i++) {
            //System.out.println(generator.nextId());
            snowFlakeIdGenerator.nextId();
        }
        System.out.println("cost " + Duration.between(start, Instant.now()));

    }

}
