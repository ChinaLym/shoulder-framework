package org.shoulder.core.log;

import ch.qos.logback.core.util.CachingDateFormatter;
import org.junit.jupiter.api.Test;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.log.logback.pattern.CachingFastDateFormatter;
import org.shoulder.core.util.DateUtils;

import java.util.concurrent.CountDownLatch;

/**
 * 带缓存的时间格式化器，去掉了锁。LocalDateTime.ofInstant 替代 new Date
 *
 * @author lym
 * @see CachingDateFormatter 可以对比 logback 的实现，logback中使用了 synchronized 代码块，而日志打印必定会有多个线程竞争，导致阻塞，Shoulder中去掉了锁
 */
public class CachingFastDateFormatterTest {


    /**
     * 生成次数（默认 1亿次，低性能机器适当调低，否则将过于卡顿或导致测试结果不准确），实际中长时间使用，shoulder实现由于带有缓存，性能会更好
     */
    private static final int GENERATE_NUM = 100_000_000;

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
     * 【性能】单线程，logback: 900-2000ms；shoulder 700-800ms
     */
    @Test
    public void timer_format() {
        CachingDateFormatter logback = new CachingDateFormatter(AppInfo.UTC_DATE_TIME_FORMAT);
        CachingFastDateFormatter shoulder = new CachingFastDateFormatter(AppInfo.UTC_DATE_TIME_FORMAT);
        DateUtils.lazyCurrentMills();
        for (int i = 0; i < GENERATE_NUM; i++) {
            long now = DateUtils.lazyCurrentMills();
            logback.format(now);
            //shoulder.format(now);
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < GENERATE_NUM; i++) {
            long now = DateUtils.lazyCurrentMills();
            logback.format(now);
            //shoulder.format(now);
        }
        long end = System.currentTimeMillis() - start;
        System.out.println("TIME TEST: " + GENERATE_NUM + " format by 1 threads cost " + end + " ms");
    }

    /**
     * 【性能】多线程，logback: 2000ms+，shoulder 280ms,且CPU占用峰值更低
     */
    @Test
    public void timer_format_threads() throws InterruptedException {
        final int totalThreadNum = THREADS;
        CountDownLatch startLatch = new CountDownLatch(totalThreadNum);
        CountDownLatch finishLatch = new CountDownLatch(totalThreadNum);
        CachingDateFormatter logback = new CachingDateFormatter(AppInfo.UTC_DATE_TIME_FORMAT);
        CachingFastDateFormatter shoulder = new CachingFastDateFormatter(AppInfo.UTC_DATE_TIME_FORMAT);
        DateUtils.lazyCurrentMills();
        for (int threadNum = 0; threadNum < totalThreadNum; threadNum++) {
            Thread t = new Thread(Thread.currentThread().getThreadGroup(),
                () -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < GENERATE_NUM / totalThreadNum; i++) {
                            long now = DateUtils.lazyCurrentMills();
                            logback.format(now);
                            //shoulder.format(now);
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
        long start = System.currentTimeMillis();
        finishLatch.await();
        long end = System.currentTimeMillis() - start;
        System.out.println("TIME TEST: " + GENERATE_NUM + " format by " + THREADS + " threads cost " + end + " ms");
    }


}
