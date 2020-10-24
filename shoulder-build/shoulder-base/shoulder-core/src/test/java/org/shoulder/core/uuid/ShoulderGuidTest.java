package org.shoulder.core.uuid;

import org.junit.Test;
import org.shoulder.core.util.JsonUtils;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * shoulder 开发的无锁化 Guid 生成器测试
 *
 * @author lym
 */
public class ShoulderGuidTest {

    /**
     * 生成 100w 次
     */
    private static final int GENERATE_NUM = 1000000;

    /**
     * 测试无重复
     */
    @Test
    public void testBaseUse() {
        LongGuidGenerator generator = new SnowFlakeGenerator(1, 1);
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM; i++) {
            //System.out.println(generator.nextId());
            generator.nextId();
        }
        System.out.println("cost " + (System.currentTimeMillis() - start));
    }

    /**
     * 测试 twitter 的翻译实现，发现 shoulder 的生成速度是 twitterSnowFlakeIdGenerator.java 的近万倍！！
     */
    @Test
    public void testTwitter() {
        SnowFlakeIdGenerator snowFlakeIdGenerator = new SnowFlakeIdGenerator(1, 1);
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM; i++) {
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
        LongGuidGenerator generator = new SnowFlakeGenerator(1, 1);
        int threadNum = 10;
        CountDownLatch latch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            new Thread(Thread.currentThread().getThreadGroup(),
                new Worker(generator, latch),
                "worker-" + i).run();
        }
        latch.countDown();
    }


    static class Worker implements Runnable {

        LongGuidGenerator generator;
        CountDownLatch latch;

        public Worker(LongGuidGenerator generator, CountDownLatch latch) {
            this.generator = generator;
            this.latch = latch;
        }

        @Override
        public void run() {
            for (int i = 0; i < GENERATE_NUM; i++) {
                System.out.println(generator.nextId());
            }
        }
    }


    /**
     * 测试自定义扩展
     * （举例：针对单机场景做性能优化）
     * 实际没必要对它做进一步性能优化：生成 100w 数据时，仅快 5ms 左右。如果你的业务量能到 100w/s 做梦都要笑醒了，况且也不会差这几毫秒
     */
    @Test
    public void testExtension_41_0_22() {
        LongGuidGenerator generator = new ShoulderGuidGenerator(
            41, System.currentTimeMillis(), 0, 0, 22);
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
            41, timeEpoch, instanceIdBits, instanceId, 12);

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
