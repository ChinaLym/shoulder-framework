package org.shoulder.core.util;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * 日期工具
 *
 * @author lym
 */
public class DateUtils {

    private static volatile long mills;

    private static volatile Instant instant;

    /**
     * 解决 linux 系统中高并发时 System.currentTimeMillis() 慢的问题（windows无影响：已经做了类似本类的事情）
     * http://pzemtsov.github.io/2017/07/23/the-slow-currenttimemillis.html
     * 使用该方法获取当前时间，无论什么系统一定比直接获取快，毫秒级别准确
     * 不过使用 4c8g 机器测试发现，当获取时间的 QPS 达到4w/s才能抵消新开线程每毫秒自增的性能消耗
     */
    public static long lazyCurrentMills() {
        return mills;
    }

    /**
     * 当前秒钟
     *
     * @return 当前秒数
     */
    public static long lazyCurrentSecond() {
        return instant.getEpochSecond();
    }

    /**
     * 当前时间
     *
     * @return 当前秒数
     */
    public static Instant lazyInstant() {
        return instant;
    }

    static {
        Thread thread = new Thread(() -> {
            while (true) {
                instant = Instant.now();
                mills = instant.toEpochMilli();
                /*if ((mills & 31) == 0) {
                    // 每 32ms 进行一次校准，避免时钟偏离

                    mills = instant.toEpochMilli();
                } else {
                    // 只有本线程写，故无需保证该操作原子
                    mills++;
                }*/
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("lazyTime");
        thread.start();
    }

}
