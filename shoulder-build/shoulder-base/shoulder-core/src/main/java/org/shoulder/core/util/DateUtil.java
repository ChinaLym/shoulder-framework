package org.shoulder.core.util;

public class DateUtil {

    private static volatile long mills;

    /**
     * 与 System.currentTimeMillis() 相比提升 5% 性能
     */
    public static long lazyCurrentMills() {
        return mills;
    }

    static {
        Thread thread = new Thread(() -> {
            int time = 0;
            while (true) {
                if (time % 1024 == 0) {
                    mills = System.currentTimeMillis();
                } else {
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
