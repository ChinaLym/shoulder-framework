package org.shoulder.core.concurrent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadsTest {

    static {
        Threads.setExecutorService(new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10)));
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        Threads.setTaskScheduler(scheduler);
    }

    @Test
    public void testSchedule() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        Threads.schedule("ut-testSchedule",
            () -> count.addAndGet(1),
            Duration.ZERO,
            (now, executionTimes) -> executionTimes == 5 ? PeriodicTask.NO_NEED_EXECUTE : now.plus(Duration.ofMillis(200))
        );

        Thread.sleep(1000 * 2);
        Assertions.assertEquals(5, count.get());
    }

}
