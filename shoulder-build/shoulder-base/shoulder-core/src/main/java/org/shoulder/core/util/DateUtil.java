package org.shoulder.core.util;

public class DateUtil {

    private static volatile long mills;

    /**
     * 解决 linux 系统中高并发时 System.currentTimeMillis() 慢的问题（windows无影响）
     * http://pzemtsov.github.io/2017/07/23/the-slow-currenttimemillis.html
     */
    public static long lazyCurrentMills() {
        return mills;
    }

    static {
        Thread thread = new Thread(() -> {
            while (true) {
                if ((mills & 1023) == 0) {
                    // 每 1024ms 进行一次校准，避免时钟偏离
                    mills = System.currentTimeMillis();
                } else {
                    // 只有本线程写，故无需保证该操作原子
                    mills++;
                }
                try {
                    Thread.sleep(1);
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
